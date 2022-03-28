package norg

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.sksamuel.hoplite.ConfigLoader
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::norg).start(wait = true)
}

fun Application.norg() {
    val config = ConfigLoader { addDefaultParsers() }.loadConfigOrThrow<Config>("/config.yml")
    val norg = NorgProxyClient(config.norg)

    runBlocking {
        norg.hentArbeidsfordeling(Arbeidsfordeling.Request("030102", false, "ANY"))
    }
}

private class NorgProxyClient(private val config: NorgConfig) {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }
        }
        install(HttpTimeout)
        install(HttpRequestRetry)
    }

    suspend fun hentArbeidsfordeling(request: Arbeidsfordeling.Request) =
        httpClient.post(config.proxyUrl) {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<Arbeidsfordeling.Response>()
}

internal object Arbeidsfordeling {
    data class Request(
        val geografiskOmraade: String,
        val skjermet: Boolean = false,
        val diskresjonskode: String = "ANY",
        val tema: String = "AAP",
        val behandlingstema: String = Behandlingstema.`§11-5`,
    )

    private object Behandlingstema {
        const val `§11-5` = "abNNNN" // TODO: bestille hos NORG
    }

    data class Response(val enhetNr: String)
}


private fun simpleHttpClient() = HttpClient(CIO) {
    install(HttpTimeout) {
        connectTimeoutMillis = 10000
        requestTimeoutMillis = 10000
        socketTimeoutMillis = 10000
    }
}
