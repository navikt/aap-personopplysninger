package nom.skjerming

import com.sksamuel.hoplite.ConfigLoader
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import java.net.URL

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::skjerming).start(wait = true)
}

internal data class Config(val skjerming: SkjermingConfig) {
    data class SkjermingConfig(val baseUrl: URL)
}

fun Application.skjerming() {
    val config = ConfigLoader { addDefaultParsers() }.loadConfigOrThrow<Config>("/config.yml")
    val client = SkjermetClient(config.skjerming)
    println(config.skjerming.baseUrl)
    runBlocking { client.erSkjermet("11111111111") }.let(::println)

    routing {
        get("actuator/healthy") { call.respondText("healthy") }
    }
}

internal class SkjermetClient(
    private val config: Config.SkjermingConfig,
    private val client: HttpClient = HttpClient(CIO) { Json {} },
) {
    suspend fun erSkjermet(personident: String): Boolean = client.post("${config.baseUrl}/skjermet") {
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
        body = SkjermingRequest(personident)
    }

    private data class SkjermingRequest(val personident: String)
}
