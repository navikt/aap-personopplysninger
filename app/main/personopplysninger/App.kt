package personopplysninger

import com.sksamuel.hoplite.ConfigLoader
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import model.Personopplysninger
import model.Personopplysninger.PersonopplysningerDto
import no.nav.aap.kafka.KafkaConfig
import no.nav.aap.kafka.serde.json.JsonSerde
import no.nav.aap.kafka.streams.*
import no.nav.aap.ktor.client.AzureConfig
import org.apache.kafka.streams.kstream.Branched
import personopplysninger.norg.NorgProxyClient
import personopplysninger.norg.ProxyConfig
import personopplysninger.norg.norgStream
import personopplysninger.pdl.api.PdlConfig
import personopplysninger.pdl.api.PdlGraphQLClient
import personopplysninger.pdl.streams.pdlStream
import personopplysninger.skjerming.SkjermetDto
import personopplysninger.skjerming.skjermingStream

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::personopplysninger).start(wait = true)
}

internal data class Config(
    val pdl: PdlConfig,
    val proxy: ProxyConfig,
    val azure: AzureConfig,
    val kafka: KafkaConfig,
)

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

fun Application.personopplysninger(
    kafka: Kafka = KStreams,
) {
    val config = ConfigLoader { addDefaultParsers() }.loadConfigOrThrow<Config>("/config.yml")
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) { registry = prometheus }

    val pdlClient = PdlGraphQLClient(config.pdl, config.azure)
    val norgClient = NorgProxyClient(config.proxy)

    kafka.start(config.kafka) {
        val personopplysninger = consume(Topics.personopplysninger)
            .filterNotNull { "skip-personopplysning-tombstone" }
            .mapValues(PersonopplysningerDto::restore)
//        val personopplysningerTable = personopplysninger.produce(Tables.personopplysninger)
        val skjermingTable = globalTable(Tables.skjerming)

        personopplysninger.split()
            .branch(erSkjermingStream, Branched.withConsumer(skjermingStream(skjermingTable)))
            .branch(erPdlStream, Branched.withConsumer(pdlStream(pdlClient)))
            .branch(erNorgStream, Branched.withConsumer(norgStream(norgClient)))

        // update streams
//        LeesahStream(pdlClient, personopplysningerTable, this)
//        GeografiskTilknytningStream(this)
    }

    routing {
        actuators(prometheus, kafka)
    }
}

private val erSkjermingStream: (_: String, value: Personopplysninger) -> Boolean = { _, personopplysning ->
    personopplysning.kanSetteSkjerming()
}

private val erPdlStream: (_: String, value: Personopplysninger) -> Boolean = { _, personopplysning ->
    personopplysning.kanSetteAdressebeskyttelse() || personopplysning.kanSetteGeografiskTilknytning()
}

private val erNorgStream: (_: String, value: Personopplysninger) -> Boolean = { _, personopplysning ->
    personopplysning.kanSetteEnhet()
}
