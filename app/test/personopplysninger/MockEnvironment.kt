package personopplysninger

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import no.nav.aap.kafka.streams.test.KafkaStreamsMock
import personopplysninger.mocks.azureAdMock
import personopplysninger.mocks.norgMock
import personopplysninger.mocks.pdlApiMock

internal fun testApp(test: suspend ApplicationTestBuilder.(mocks: MockEnvironment) -> Unit) =
    MockEnvironment().use { mocks ->
        testApplication {
            environment { config = mocks.environmentVariables }
            application {
                server(mocks.kafka)
                runBlocking { this@testApplication.test(mocks) }
            }
        }
    }

internal class MockEnvironment : AutoCloseable {
    private val norg = embeddedServer(Netty, port = 0, module = Application::norgMock).apply { start() }
    private val pdl = embeddedServer(Netty, port = 0, module = Application::pdlApiMock).apply { start() }
    private val oauth = embeddedServer(Netty, port = 0, module = Application::azureAdMock).apply { start() }
    val kafka = KafkaStreamsMock()

    // Setter properties som brukes før ktor blir involvert
    // Kotlin sin "object" blir instansiert før ktor, og AktorAvroSerde bruker System.getenv/System.getProperty
    init {
        System.setProperty("KAFKA_SCHEMA_REGISTRY", "mock://schema-reg.test")
        System.setProperty("KAFKA_SCHEMA_REGISTRY_USER", "")
        System.setProperty("KAFKA_SCHEMA_REGISTRY_PASSWORD", "")
        System.setProperty("KAFKA_TRUSTSTORE_PATH", "")
        System.setProperty("KAFKA_KEYSTORE_PATH", "")
        System.setProperty("KAFKA_CREDSTORE_PASSWORD", "")
    }

    val environmentVariables = MapApplicationConfig(
        "AZURE_OPENID_CONFIG_TOKEN_ENDPOINT" to "http://localhost:${oauth.port}/token",
        "AZURE_APP_CLIENT_ID" to "test",
        "AZURE_APP_CLIENT_SECRET" to "test",
        "NORG_URL" to "http://localhost:${norg.port}/norg2",
        "PDL_URL" to "http://localhost:${pdl.port}/graphql",
        "PDL_SCOPE" to "test",
        "KAFKA_STREAMS_APPLICATION_ID" to "personopplysninger",
        "KAFKA_BROKERS" to "mock://kafka",
        "KAFKA_TRUSTSTORE_PATH" to "",
        "KAFKA_KEYSTORE_PATH" to "",
        "KAFKA_CREDSTORE_PASSWORD" to "",
        "KAFKA_CLIENT_ID" to "personopplysninger",
        "KAFKA_SCHEMA_REGISTRY" to "mock://schema-reg.test",
        "KAFKA_SCHEMA_REGISTRY_USER" to "",
        "KAFKA_SCHEMA_REGISTRY_PASSWORD" to "",
    )

    companion object {
        val NettyApplicationEngine.port get() = runBlocking { resolvedConnectors() }.first { it.type == ConnectorType.HTTP }.port
    }

    override fun close() {
        norg.stop(0L, 0L)
        pdl.stop(0L, 0L)
        oauth.stop(0L, 0L)
        kafka.close()
    }
}