package pdl.api

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*

internal data class PdlConfig(val url: URI, val scope: String)

private val log = LoggerFactory.getLogger(PdlClient::class.java)

internal class PdlClient(
    private val client: HttpClient,
    private val config: PdlConfig,
    private val azureClient: AzureClient,
) {
    suspend fun hentPersonopplysninger(personident: String): PdlData? =
        client.post<PdlResponse>(config.url.toURL()) {
            accept(ContentType.Application.Json)
            header("Authorization", "Bearer ${azureClient.getToken(config.scope).accessToken}")
            header("Nav-Call-Id", callId)
            header("TEMA", "AAP")
            contentType(ContentType.Application.Json)
            body = query(personident)
        }.let { response ->
            if (response.errors != null) null.also { log.error("Feil fra PDL, ${response.errors}") }
            else response.data
        }

    private val callId: String get() = UUID.randomUUID().toString().also { println("calling pdl with call-id $it") }
}