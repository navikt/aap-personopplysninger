package pdl.api

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

internal data class AzureConfig(val tokenEndpoint: URL, val clientId: String, val clientSecret: String)

internal class AzureClient(private val config: AzureConfig) {

    companion object {
        private val objectMapper = ObjectMapper()
    }

    private val tokencache: MutableMap<String, Token> = mutableMapOf()

    fun getToken(scope: String): Token =
        tokencache[scope]?.takeUnless(Token::isExpired) ?: fetchToken(scope).also { token -> tokencache[scope] = token }

    private fun fetchToken(scope: String): Token {
        val (responseCode, responseBody) = with(config.tokenEndpoint.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            doOutput = true
            outputStream.use {
                it.bufferedWriter().apply {
                    write("client_id=${config.clientId}&client_secret=${config.clientSecret}&scope=$scope&grant_type=client_credentials")
                    flush()
                }
            }

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.bufferedReader()?.readText()
        }

        if (responseBody == null) error("ukjent feil fra azure ad (responseCode=$responseCode), responseBody er null")
        val jsonNode = objectMapper.readTree(responseBody)

        if (jsonNode.has("error")) error("error from the azure token endpoint: ${jsonNode["error_description"].textValue()}")
        else if (responseCode >= 300) error("unknown error (responseCode=$responseCode) from azure ad")

        return Token(
            tokenType = jsonNode["token_type"].textValue(),
            expiresIn = jsonNode["expires_in"].longValue(),
            accessToken = jsonNode["access_token"].textValue()
        )
    }

    data class Token(val tokenType: String, val expiresIn: Long, val accessToken: String) {
        companion object {
            private const val leewaySeconds = 60
        }

        private val expiresOn = Instant.now().plusSeconds(expiresIn - leewaySeconds)

        fun isExpired(): Boolean = expiresOn.isBefore(Instant.now())
    }
}
