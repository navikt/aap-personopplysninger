package personopplysninger.mocks

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.intellij.lang.annotations.Language

internal fun Application.fssProxyMock() {
    install(ContentNegotiation) { jackson {} }
    routing {
        post("/api/v1/arbeidsfordeling/enheter/bestmatch") {
            call.respondText(lokalkontor, ContentType.Application.Json)
        }
    }
}

@Language("JSON")
private const val lokalkontor = """{ "enhetNr" : "4201"}"""
