package personopplysninger.pdl.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import pdl.api.AzureClient
import java.net.URI
import java.util.*

internal data class PdlConfig(
    val url: URI,
    val scope: String,
)

internal class PdlGraphQLClient(
    private val config: PdlConfig,
    private val azureClient: AzureClient,
) {
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

    suspend fun hentAlt(personident: String) = query(PdlRequest.hentAlt(personident))
    suspend fun hentAdressebeskyttelse(personident: String) = query(PdlRequest.hentAdressebeskyttelse(personident))

    private suspend fun query(query: PdlRequest): PdlResponse =
        httpClient.post(config.url.toURL()) {
            accept(ContentType.Application.Json)
            header("Authorization", "Bearer ${azureClient.getToken(config.scope).accessToken}")
            header("Nav-Call-Id", callId)
            header("TEMA", "AAP")
            contentType(ContentType.Application.Json)
            setBody(query)
        }.body<PdlResponse>()
            .also { response ->
                // graphql valideringsfeil
                if (response.errors != null) error("Feil fra PDL, ${response.errors}")
            }

    private val callId: String get() = UUID.randomUUID().toString().also { println("calling pdl with call-id $it") }
}
