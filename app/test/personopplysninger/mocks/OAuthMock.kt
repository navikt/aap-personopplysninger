package personopplysninger.mocks

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.intellij.lang.annotations.Language

internal fun oauthMock() = embeddedServer(Netty, port = 0) {
    install(ContentNegotiation) { jackson { enable(SerializationFeature.INDENT_OUTPUT) } }
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
  "ext_expires_in": 3599,
  "access_token": "very.secure.token"
}
"""
