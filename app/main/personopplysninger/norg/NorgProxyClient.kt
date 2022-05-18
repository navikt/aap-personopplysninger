package personopplysninger.norg

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
import personopplysninger.pdl.api.PdlGraphQLClient
import java.net.URL

internal data class ProxyConfig(val baseUrl: URL)

private val secureLog = LoggerFactory.getLogger("secureLog")
private val log = LoggerFactory.getLogger(NorgProxyClient::class.java)

internal class NorgProxyClient(private val config: ProxyConfig) {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    log.info(message) // fixme: secureLog instead
                }
            }
        }
        install(HttpTimeout)
        install(HttpRequestRetry)
    }

    suspend fun hentArbeidsfordeling(request: ArbeidsfordelingRequest) =
        httpClient.post("${config.baseUrl}/norg/arbeidsfordeling") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<ArbeidsfordelingResponse>()
}
