package personopplysninger.mocks

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.intellij.lang.annotations.Language

internal fun norgProxyMock() = embeddedServer(Netty, port = 0) {
    install(ContentNegotiation) { jackson { enable(SerializationFeature.INDENT_OUTPUT) } }
    routing {
        post("/norg/arbeidsfordeling") {
            call.respondText(lokalkontor, ContentType.Application.Json)
        }
    }
}

@Language("JSON")
private const val lokalkontor = """
{
  "enhetNr" : "4201"
}    
"""
