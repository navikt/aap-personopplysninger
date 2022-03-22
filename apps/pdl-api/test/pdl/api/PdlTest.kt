package pdl.api

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.testing.*
import org.intellij.lang.annotations.Language
import org.junit.Test

class PdlTest {

    @Test
    fun test() {
        Mocks.use {
            withTestApplication(Application::pdlApi) {}
        }
    }
}

object Mocks : AutoCloseable {
    private val pdl = pdlMock().apply { start() }
    private val oauth = oauthMock().apply { start() }
    override fun close() {
        pdl.stop(100, 100)
        oauth.stop(100, 100)
    }

    private fun pdlMock() = embeddedServer(Netty, port = 8099) {
        install(ContentNegotiation) { jackson { enable(SerializationFeature.INDENT_OUTPUT) } }
        routing {
            post("/graphql") {
                require(call.request.headers["Authorization"] == "Bearer very.secure.token")
                val requested = call.receive<GraphqlQuery>()
                when (requested.variables.ident) {
                    "11111111111" -> call.respondText(pdlStrengtFortroligResponse, ContentType.Application.Json)
                    else -> call.respondText(pdlGeografiskTilknytningResponse, ContentType.Application.Json)
                }
            }
        }
    }

    private fun oauthMock() = embeddedServer(Netty, port = 8098) {
        install(ContentNegotiation) { jackson { enable(SerializationFeature.INDENT_OUTPUT) } }
        routing {
            post("/token") {
                require(call.receiveText() == "client_id=test&client_secret=test&scope=test&grant_type=client_credentials")
                call.respondText(azureTokenResponse, ContentType.Application.Json)
            }
        }
    }
}

@Language("JSON")
private const val pdlGeografiskTilknytningResponse = """
{
  "data": {
    "hentGeografiskTilknytning": {
      "gtKommune": "4632",
      "gtBydel": null,
      "gtLand": null
    },
    "hentPerson": {
      "adressebeskyttelse": []
    }
  },
  "extensions": {}
}
"""

@Language("JSON")
private const val pdlStrengtFortroligResponse = """
{
  "data": {
    "hentGeografiskTilknytning": {
      "gtKommune": null,
      "gtBydel": null,
      "gtLand": null
    },
    "hentPerson": {
      "adressebeskyttelse": [
        {
          "gradering": "STRENGT_FORTROLIG"
        }
      ]
    }
  },
  "extensions": {}
}
"""

@Language("JSON")
private const val azureTokenResponse = """
{
  "token_type": "Bearer",
  "expires_in": 3599,
  "ext_expires_in": 3599,
  "access_token": "very.secure.token"
}
"""
