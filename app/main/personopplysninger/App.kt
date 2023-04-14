package personopplysninger

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.aap.kafka.streams.v2.Streams
import no.nav.aap.kafka.streams.v2.KafkaStreams
import no.nav.aap.kafka.streams.v2.topology
import no.nav.aap.ktor.config.loadConfig
import personopplysninger.graphql.PdlGraphQLClient
import personopplysninger.kafka.Tables
import personopplysninger.rest.NorgClient
import personopplysninger.streams.aktørStream
import personopplysninger.streams.personopplysningStream
import personopplysninger.streams.søknadStream

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

fun Application.server(kafka: Streams = KafkaStreams()) {
    val config = loadConfig<Config>()
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) { registry = prometheus }

    val pdlClient = PdlGraphQLClient(config.pdl, config.azure)
    val norgClient = NorgClient(config.norg)

    environment.monitor.subscribe(ApplicationStopping) { kafka.close() }

    kafka.connect(
        topology = topology(pdlClient, norgClient, config.toggle.settOppAktørStream),
        config = config.kafka,
        registry = prometheus,
    )

    routing {
        actuators(prometheus, kafka)
    }
}

internal fun topology(pdlClient: PdlGraphQLClient, norgClient: NorgClient, settOppAktørStream: Boolean) =
    topology {
        val skjermingKTable = consumeRepartitioned(Tables.skjerming)

        val søkereKTable = consume(Tables.søkere)

        if (settOppAktørStream) {
            aktørStream(søkereKTable)
        }

        søknadStream()
        personopplysningStream(skjermingKTable, pdlClient, norgClient)
    }
