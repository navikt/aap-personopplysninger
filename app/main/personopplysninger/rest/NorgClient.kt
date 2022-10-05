package personopplysninger.rest

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import org.slf4j.LoggerFactory
import java.net.URL

internal data class NorgConfig(val url: URL)

private val secureLog = LoggerFactory.getLogger("secureLog")

internal class NorgClient(private val config: NorgConfig) {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }
        }
        install(Logging) {
            level = LogLevel.BODY
            logger = object : Logger {
                override fun log(message: String) = secureLog.info(message)
            }
        }
        install(HttpTimeout)
        install(HttpRequestRetry)
    }

    suspend fun hentArbeidsfordeling(request: ArbeidsfordelingDtoRequest) =
        httpClient.post("${config.url}/api/v1/arbeidsfordeling/enheter/bestmatch") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<List<ArbeidsfordelingDtoResponse>>()
}
