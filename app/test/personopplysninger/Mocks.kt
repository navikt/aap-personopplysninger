package personopplysninger

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
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Language
import personopplysninger.pdl.api.PdlRequest

class Mocks : AutoCloseable {
    val pdl = pdlMock().apply { start() }
    val oauth = oauthMock().apply { start() }
    val norg = norgProxyMock().apply { start() }
    val kafka = KStreamsMock()

    companion object {
        val NettyApplicationEngine.port get() = runBlocking { resolvedConnectors() }.first { it.type == ConnectorType.HTTP }.port
    }

    override fun close() {
        pdl.stop(100, 100)
        oauth.stop(100, 100)
        norg.stop(100, 100)
        kafka.close()
    }

    private fun norgProxyMock() = embeddedServer(Netty, port = 0) {
        install(ContentNegotiation) { jackson { enable(SerializationFeature.INDENT_OUTPUT) } }
        routing {
            post("/norg/arbeidsfordeling") {
                call.respondText(norgArbeidsfordelingResponse, ContentType.Application.Json)
            }
        }
    }

    private fun oauthMock() = embeddedServer(Netty, port = 0) {
        install(ContentNegotiation) { jackson { enable(SerializationFeature.INDENT_OUTPUT) } }
        routing {
            post("/token") {
                require(call.receiveText() == "client_id=test&client_secret=test&scope=test&grant_type=client_credentials")
                call.respondText(azureTokenResponse, ContentType.Application.Json)
            }
        }
    }

    private fun pdlMock() = embeddedServer(Netty, port = 0) {
        install(ContentNegotiation) { jackson { enable(SerializationFeature.INDENT_OUTPUT) } }
        routing {
            post("/graphql") {
                require(call.request.headers["Authorization"] == "Bearer very.secure.token")
                val requested = call.receive<PdlRequest>()
                when (requested.variables.ident) {
                    "11111111111" -> call.respondText(pdlStrengtFortroligResponse, ContentType.Application.Json)
                    else -> call.respondText(pdlGeografiskTilknytningResponse, ContentType.Application.Json)
                }
            }
        }
    }
}

@Language("JSON")
private const val norgArbeidsfordelingResponse = """
{
  "enhetNr" : "4201"
}    
"""

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
