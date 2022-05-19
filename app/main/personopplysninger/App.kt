package personopplysninger

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.aap.kafka.serde.json.JsonSerde
import no.nav.aap.kafka.streams.*
import no.nav.aap.ktor.config.loadConfig
import org.apache.kafka.streams.kstream.Branched
import personopplysninger.Personopplysninger.PersonopplysningerDto
import personopplysninger.norg.NorgClient
import personopplysninger.norg.norgStream
import personopplysninger.pdl.api.PdlGraphQLClient
import personopplysninger.pdl.streams.leesahStream
import personopplysninger.pdl.streams.pdlStream
import personopplysninger.skjerming.SkjermetDto
import personopplysninger.skjerming.skjermingStream

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

object Topics {
    //    val leesah = Topic("aapen-person-pdl-leesah-v1", AvroSerde.generic())
    val skjerming = Topic("nom.skjermede-personer-v1", JsonSerde.jackson<SkjermetDto>())
    val personopplysninger = Topic("aap.personopplysninger.v1", JsonSerde.jackson<PersonopplysningerDto>())
//    val geografiskTilknytning = Topic("aapen-pdl-geografisktilknytning-v1", JsonSerde.jackson<String>())
}

object Tables {
    val personopplysninger = Table("personopplysninger", Topics.personopplysninger)
    val skjerming = Table("skjerming", Topics.skjerming, true)
}

fun Application.server(kafka: KStreams = KafkaStreams) {
    val config = loadConfig<Config>()
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) { registry = prometheus }

    val pdlClient = PdlGraphQLClient(config.pdl, config.azure)
    val norgClient = NorgClient(config.norg)

    kafka.start(config.kafka, prometheus) {
        val personopplysninger = consume(Topics.personopplysninger)
            .filterNotNull("skip-personopplysning-tombstone")
            .mapValues(PersonopplysningerDto::restore)
//        val personopplysningerTable = personopplysninger.produce(Tables.personopplysninger)
        val skjermingTable = globalTable(Tables.skjerming)

        personopplysninger.split()
            .branch(isSkjermingStream, Branched.withConsumer(skjermingStream(skjermingTable)))
            .branch(isPdlStream, Branched.withConsumer(pdlStream(pdlClient)))
            .branch(isNorgStream, Branched.withConsumer(norgStream(norgClient)))

        // update streams
        leesahStream()
//        GeografiskTilknytningStream(this)
    }

    routing {
        actuators(prometheus, kafka)
    }
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
