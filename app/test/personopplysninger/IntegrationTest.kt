package personopplysninger

import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import no.nav.aap.kafka.streams.test.KafkaStreamsMock
import no.nav.aap.kafka.streams.test.readAndAssert
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import personopplysninger.domain.PersonopplysningerDto
import personopplysninger.kafka.Topics
import personopplysninger.streams.SkjermetDto
import java.time.LocalDateTime

internal class IntegrationTest {

    @Test
    @Disabled("Kun for debug i miljø med ident mot PDL")
    fun `test integration`() = testApp { mocks ->
        val skjermingInput = mocks.kafka.inputTopic(Topics.skjerming)
        val personopplysningerInput = mocks.kafka.inputTopic(Topics.personopplysninger)
        val personopplysningerOutput = mocks.kafka.outputTopic(Topics.personopplysninger)

        val ident = ""
        skjermingInput.pipeInput(ident, SkjermetDto(LocalDateTime.now().minusDays(30), null))
        personopplysningerInput.pipeInput(ident, PersonopplysningerDto())

        personopplysningerOutput.readAndAssert()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(ident, 3)
    }


    private fun testApp(test: suspend ApplicationTestBuilder.(mocks: MockEnvironment) -> Unit) =
        MockEnvironment().use { mocks ->
            testApplication {
                environment { config = mocks.environmentVariables }
                application {
                    server(mocks.kafka)
                    runBlocking { this@testApplication.test(mocks) }
                }
            }
        }

    private class MockEnvironment : AutoCloseable {
        val kafka = KafkaStreamsMock()

        override fun close() {
            kafka.close()
        }

        // HOWTO: fyll azure stuff disse fra k8s secrets
        val environmentVariables = MapApplicationConfig(
            "AZURE_OPENID_CONFIG_TOKEN_ENDPOINT" to "",
            "AZURE_APP_CLIENT_ID" to "",
            "AZURE_APP_CLIENT_SECRET" to "",

            "NORG_URL" to "https://norg2.dev-fss-pub.nais.io/norg2",
            "PDL_URL" to "https://pdl-api.dev.intern.nav.no/graphql",
            "PDL_SCOPE" to "api://dev-fss.pdl.pdl-api/.default",

            "KAFKA_STREAMS_APPLICATION_ID" to "personopplysninger",
            "KAFKA_BROKERS" to "mock://kafka",
            "KAFKA_TRUSTSTORE_PATH" to "",
            "KAFKA_KEYSTORE_PATH" to "",
            "KAFKA_CREDSTORE_PASSWORD" to "",
            "KAFKA_CLIENT_ID" to "personopplysninger",
        )
    }
}