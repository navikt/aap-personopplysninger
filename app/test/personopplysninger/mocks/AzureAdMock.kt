package personopplysninger.mocks

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.intellij.lang.annotations.Language

internal fun Application.azureAdMock() {
    install(ContentNegotiation) { jackson {} }
    routing {
        post("/token") {
            require(call.receiveText() == "client_id=test&client_secret=test&scope=test&grant_type=client_credentials")
            call.respondText(validToken, ContentType.Application.Json)
        }
    }
}

@Language("JSON")
private const val validToken = """
{
  "token_type": "Bearer",
  "expires_in": 3599,
  "access_token": "very.secure.token"
}
"""
