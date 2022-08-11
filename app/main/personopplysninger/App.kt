package personopplysninger

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.aap.kafka.serde.json.JsonSerde
import no.nav.aap.kafka.streams.KStreams
import no.nav.aap.kafka.streams.KafkaStreams
import no.nav.aap.kafka.streams.Table
import no.nav.aap.kafka.streams.Topic
import no.nav.aap.kafka.streams.extension.consume
import no.nav.aap.kafka.streams.extension.filterNotNull
import no.nav.aap.kafka.streams.extension.globalTable
import no.nav.aap.ktor.config.loadConfig
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Branched
import personopplysninger.Personopplysninger.PersonopplysningerDto
import personopplysninger.norg.NorgClient
import personopplysninger.norg.norgStream
import personopplysninger.pdl.api.PdlGraphQLClient
import personopplysninger.pdl.streams.leesahStream
import personopplysninger.pdl.streams.pdlStream
import personopplysninger.skjerming.SkjermetDto
import personopplysninger.skjerming.skjermingStream
import personopplysninger.søknad.SøknadDto
import personopplysninger.søknad.søknadStream

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

object Topics {
    //    val leesah = Topic("aapen-person-pdl-leesah-v1", AvroSerde.generic())
    val skjerming = Topic("nom.skjermede-personer-v1", JsonSerde.jackson<SkjermetDto>())
    val personopplysninger = Topic("aap.personopplysninger.v1", JsonSerde.jackson<PersonopplysningerDto>())
    val søknad = Topic("aap.soknad-sendt.v1", JsonSerde.jackson<SøknadDto>())
//    val geografiskTilknytning = Topic("aapen-pdl-geografisktilknytning-v1", JsonSerde.jackson<String>())
}

object Tables {
    //    val personopplysninger = Table("personopplysninger", Topics.personopplysninger)
    val skjerming = Table("skjerming", Topics.skjerming, true)
}

fun Application.server(kafka: KStreams = KafkaStreams) {
    val config = loadConfig<Config>()
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) { registry = prometheus }

    val pdlClient = PdlGraphQLClient(config.pdl, config.azure)
    val norgClient = NorgClient(config.norg)

    kafka.connect(
        config = config.kafka,
        registry = prometheus,
        topology = topology(pdlClient, norgClient)
    )

    routing {
        actuators(prometheus, kafka)
    }
}

internal fun topology(pdlClient: PdlGraphQLClient, norgClient: NorgClient): Topology {
    val streams = StreamsBuilder()

    val skjermingGlobalKTable = streams.globalTable(Tables.skjerming)

    streams.søknadStream()

    streams.consume(Topics.personopplysninger)
        .filterNotNull("skip-personopplysning-tombstone")
        .mapValues(PersonopplysningerDto::restore)
        .split()
        .branch(isSkjermingStream, Branched.withConsumer(skjermingStream(skjermingGlobalKTable)))
        .branch(isPdlStream, Branched.withConsumer(pdlStream(pdlClient)))
        .branch(isNorgStream, Branched.withConsumer(norgStream(norgClient)))

    // update streams
    streams.leesahStream()
//        GeografiskTilknytningStream(this)
    return streams.build()
}

private val isSkjermingStream: (_: String, value: Personopplysninger) -> Boolean = { _, personopplysning ->
    personopplysning.kanSetteSkjerming()
}

private val isPdlStream: (_: String, value: Personopplysninger) -> Boolean = { _, personopplysning ->
    personopplysning.kanSetteAdressebeskyttelse() || personopplysning.kanSetteGeografiskTilknytning()
}

private val isNorgStream: (_: String, value: Personopplysninger) -> Boolean = { _, personopplysning ->
    personopplysning.kanSetteEnhet()
}
