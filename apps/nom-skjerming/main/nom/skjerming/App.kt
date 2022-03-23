package nom.skjerming

import com.sksamuel.hoplite.ConfigLoader
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.aap.kafka.*
import no.nav.aap.kafka.streams.KStreams
import no.nav.aap.kafka.streams.Kafka
import no.nav.aap.kafka.streams.consume
import no.nav.aap.kafka.streams.produce

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::skjerming).start(wait = true)
}

internal data class Config(
    val kafka: KafkaConfig,
)

fun Application.skjerming(kafkaStreams: Kafka = KStreams) {
    val config = ConfigLoader { addDefaultParsers() }.loadConfigOrThrow<Config>("/config.yml")

    kafkaStreams.start(config.kafka) {
        consume(Topics.skjerming) { "skjerming-consumed" }
            .produce(Tables.skjerming) { "skjerming-as-ktable" }
    }

    routing {
        get("actuator/healthy") { call.respondText("healthy") }
    }
}
