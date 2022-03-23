package nom.skjerming

import com.sksamuel.hoplite.ConfigLoader
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import nom.skjerming.kafka.*

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::skjerming).start(wait = true)
}

internal data class Config(
    val kafka: KafkaConfig,
)

fun Application.skjerming(kafka: Kafka = KafkaSetup()) {
    val config = ConfigLoader { addDefaultParsers() }.loadConfigOrThrow<Config>("/config.yml")

    kafka.start(config.kafka) {
        consume(Topics.skjerming) { "skjerming-consumed" }
            .toTable(Tables.skjerming)
    }

    routing {
        get("actuator/healthy") { call.respondText("healthy") }
    }
}
