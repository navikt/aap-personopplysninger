package personopplysninger.pdl.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import no.nav.aap.ktor.client.AzureConfig
import no.nav.aap.ktor.client.HttpClientAzureAdInterceptor.Companion.azureAD
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*

internal data class PdlConfig(
    val url: URI,
    val scope: String,
)

private val log = LoggerFactory.getLogger(PdlGraphQLClient::class.java)

internal class PdlGraphQLClient(private val pdlConfig: PdlConfig, private val azureConfig: AzureConfig) {
    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout)
        install(HttpRequestRetry)
        install(Auth) { azureAD(azureConfig, pdlConfig.scope) }
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }
        }
    }

    suspend fun hentAlt(personident: String) = query(PdlRequest.hentAlt(personident))
    suspend fun hentAdressebeskyttelse(personident: String) = query(PdlRequest.hentAdressebeskyttelse(personident))

    private suspend fun query(query: PdlRequest): PdlResponse =
        httpClient.post(pdlConfig.url.toURL()) {
            accept(ContentType.Application.Json)
            header("Nav-Call-Id", callId)
            header("TEMA", "AAP")
            contentType(ContentType.Application.Json)
            setBody(query)
        }.body<PdlResponse>()
            .also { response ->
                // graphql valideringsfeil
                if (response.errors != null) error("Feil fra PDL, ${response.errors}")
            }

    private val callId: String get() = UUID.randomUUID().toString().also { log.info("calling pdl with call-id $it") }
}
