package personopplysninger

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import no.nav.aap.kafka.streams.KStreams
import no.nav.aap.kafka.streams.KafkaStreams
import no.nav.aap.kafka.streams.extension.consume
import no.nav.aap.kafka.streams.extension.filterNotNull
import no.nav.aap.kafka.streams.extension.produce
import no.nav.aap.ktor.config.loadConfig
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Repartitioned
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import personopplysninger.graphql.PdlGraphQLClient
import personopplysninger.kafka.Tables
import personopplysninger.kafka.Topics
import personopplysninger.rest.NorgClient
import personopplysninger.streams.*
import personopplysninger.streams.personopplysningStream
import personopplysninger.streams.søknadStream

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
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
        topology = topology(pdlClient, norgClient, config.toggle.settOppAktørStream)
    )

    runBlocking {
        kafka.isStoreReady<ByteArray>(Tables.søkere.stateStoreName)
    }

    routing {
        actuators(prometheus, kafka)
    }
}

internal fun topology(pdlClient: PdlGraphQLClient, norgClient: NorgClient, settOppAktørStream: Boolean): Topology {
    val streams = StreamsBuilder()

    val skjermedeTable = streams
        .consume(Topics.skjerming)
        .filterNotNull("skip-skjerming-tombstone")
        .repartition(Repartitioned.with(Topics.skjerming.keySerde, Topics.skjerming.valueSerde))
        .let {
            // it.skjermingStream() // reiniti personopplysning med skjermign
            it.produce(Tables.skjerming) // lager ktable
        }

    streams.consume(Topics.søkere)
        .mapValues { _ -> "".toByteArray() }
        .produce(Tables.søkere, true)

    if(settOppAktørStream) {
        streams.aktørStream()
    }
    streams.søknadStream()
    streams.personopplysningStream(skjermedeTable, pdlClient, norgClient)
//    streams.geografiskTilknytningStream()

//    streams.leesahStream()

    return streams.build()
}

suspend fun <V> KStreams.isStoreReady(name: String): ReadOnlyKeyValueStore<String, V> {
    val store = withTimeout(10_000L) {
        flow {
            while (true) {
                runCatching { getStore<V>(name) }
                    .getOrNull()?.let { emit(it) }
                delay(100)
            }
        }.firstOrNull()
    }
    return store ?: error("Store klarte ikke starte")
}

