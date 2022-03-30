package personopplysninger.norg

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import java.net.URL

internal data class ProxyConfig(val baseUrl: URL)

internal class NorgProxyClient(private val config: ProxyConfig) {
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

    suspend fun hentArbeidsfordeling(request: ArbeidsfordelingRequest) =
        httpClient.post("${config.baseUrl}/norg/arbeidsfordeling") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<ArbeidsfordelingResponse>()
}
