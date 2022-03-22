package pdl.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.sksamuel.hoplite.ConfigLoader
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::pdlApi).start(wait = true)
}

internal data class Config(val pdl: PdlConfig, val azure: AzureConfig)

fun Application.pdlApi() {
    val config = ConfigLoader { addDefaultParsers() }.loadConfigOrThrow<Config>("/config.yml")
    val azureClient = AzureClient(config.azure)
    val pdlClient = PdlClient(simpleHttpClient(), config.pdl, azureClient)

    runBlocking { pdlClient.hentPersonopplysninger("11111111111") }.let(::println)

    routing {
        get("actuator/healthy") {
            call.respondText("healthy")
        }
    }
}

private fun simpleHttpClient() = HttpClient(CIO) {
    install(HttpTimeout) {
        connectTimeoutMillis = 10000
        requestTimeoutMillis = 10000
        socketTimeoutMillis = 10000
    }

    install(JsonFeature) {
        serializer = JacksonSerializer {
            registerModule(JavaTimeModule())
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }
}
