package personopplysninger.pdl.api

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import pdl.api.AzureClient
import java.net.URI
import java.util.*

internal data class PdlConfig(
    val url: URI,
    val scope: String,
)

internal class PdlGraphQLClient(
    private val client: HttpClient,
    private val config: PdlConfig,
    private val azureClient: AzureClient,
) {
    suspend fun hentAlt(personident: String) = query(PdlRequest.hentAlt(personident))
    suspend fun hentAdressebeskyttelse(personident: String) = query(PdlRequest.hentAdressebeskyttelse(personident))

    private suspend fun query(query: PdlRequest): PdlResponse =
        client.post<PdlResponse>(config.url.toURL()) {
            accept(ContentType.Application.Json)
            header("Authorization", "Bearer ${azureClient.getToken(config.scope).accessToken}")
            header("Nav-Call-Id", callId)
            header("TEMA", "AAP")
            contentType(ContentType.Application.Json)
            body = query
        }.also { response ->
            if (response.errors != null) error("Feil fra PDL, ${response.errors}")
        }

    private val callId: String get() = UUID.randomUUID().toString().also { println("calling pdl with call-id $it") }
}
