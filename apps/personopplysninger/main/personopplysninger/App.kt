package personopplysninger

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.sksamuel.hoplite.ConfigLoader
import domain.Personopplysninger
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import no.nav.aap.kafka.serde.AvroSerde
import no.nav.aap.kafka.serde.JsonSerde
import no.nav.aap.kafka.streams.*
import pdl.api.AzureClient
import personopplysninger.pdl.api.PdlGraphQLClient
import personopplysninger.pdl.geografisktilknytning.GeografiskTilknytningStream
import personopplysninger.pdl.leesah.LeesahStream

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::pdlApi).start(wait = true)
}

object Topics {
    val leesah = Topic("aapen-person-pdl-leesah-v1", AvroSerde.generic())
    val personopplysninger = Topic("aap.personopplysninger.v1", JsonSerde.jackson<Personopplysninger>())
    val geografiskTilknytning = Topic("aapen-pdl-geografisktilknytning-v1", JsonSerde.jackson<String>())
}

object Tables {
    val personopplysninger = Table("personopplysninger", Topics.personopplysninger)
}

fun Application.pdlApi(kStreams: Kafka = KStreams) {
    val config = ConfigLoader { addDefaultParsers() }.loadConfigOrThrow<Config>("/config.yml")
    val azureClient = AzureClient(config.azure)
    val pdlClient = PdlGraphQLClient(simpleHttpClient(), config.pdl, azureClient)
    runBlocking { pdlClient.hentAlt("11111111111") }.let(::println)

    kStreams.start(config.kafka) {
        val personopplysningerTable = consume(Topics.personopplysninger) { "consume-personopplysninger" }
            .produce(Tables.personopplysninger, false) { "personopplysning-as-ktable" }

        LeesahStream(pdlClient, personopplysningerTable, this)
        GeografiskTilknytningStream(this)
    }

    routing {
        get("actuator/healthy") {
            call.respondText("healthy")
        }
    }
}

private fun simpleHttpClient() = HttpClient(CIO) {
    install(HttpTimeout) {
        connectTimeoutMillis = 10000
        requestTimeoutMillis = 10000
        socketTimeoutMillis = 10000
    }

    install(JsonFeature) {
        serializer = JacksonSerializer {
            registerModule(JavaTimeModule())
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }
}
