package personopplysninger

import io.ktor.server.testing.*
import model.Personopplysninger.PersonopplysningerDto
import model.Personopplysninger.SkjermingDto
import org.junit.Assert.assertEquals
import org.junit.Test
import personopplysninger.Mocks.Companion.port
import personopplysninger.skjerming.SkjermetDto
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import java.time.LocalDate
import java.time.LocalDateTime

class PersonopplysningerTest {

    @Test
    fun `skjermet person uten tom-dato joines med personopplysninger`() = testApp { mocks ->
        val skjermingInput = mocks.kafka.inputTopic(Topics.skjerming)
        val personopplysningerInput = mocks.kafka.inputTopic(Topics.personopplysninger)
        val personopplysningerOutput = mocks.kafka.outputTopic(Topics.personopplysninger)

        skjermingInput.pipeInput("12345678910", SkjermetDto(LocalDateTime.now().minusDays(30), null))
        personopplysningerInput.pipeInput("12345678910", PersonopplysningerDto())

        val oppdatertPerson = personopplysningerOutput.readKeyValue()
        val expectedSkjerming = SkjermingDto(true, LocalDate.now().minusDays(30), null)
        val expectedPersonopplysninger = PersonopplysningerDto(skjerming = expectedSkjerming)
        assertEquals("12345678910", oppdatertPerson.key)
        assertEquals(expectedPersonopplysninger, oppdatertPerson.value)
    }

    @Test
    fun `person uten skjerming joines med personopplysninger`() = testApp { mocks ->
        val personopplysningerInput = mocks.kafka.inputTopic(Topics.personopplysninger)
        val personopplysningerOutput = mocks.kafka.outputTopic(Topics.personopplysninger)

        personopplysningerInput.pipeInput("12345678910", PersonopplysningerDto())

        val oppdatertPerson = personopplysningerOutput.readKeyValue()
        val expectedSkjerming = SkjermingDto(false, null, null)
        val expectedPersonopplysninger = PersonopplysningerDto(skjerming = expectedSkjerming)
        assertEquals("12345678910", oppdatertPerson.key)
        assertEquals(expectedPersonopplysninger, oppdatertPerson.value)
    }

    @Test
    fun `kaller pdl etter at skjerming er joinet`() = testApp { mocks ->
        val skjermingInput = mocks.kafka.inputTopic(Topics.skjerming)
        val personopplysningerInput = mocks.kafka.inputTopic(Topics.personopplysninger)
        val personopplysningerOutput = mocks.kafka.outputTopic(Topics.personopplysninger)

        skjermingInput.pipeInput("12345678910", SkjermetDto(LocalDateTime.now().minusDays(30), null))
        personopplysningerInput.pipeInput("12345678910", PersonopplysningerDto())

        val first = personopplysningerOutput.readValue()
        val expectedPersonopplysninger = first.copy(geografiskTilknytning = "4632", adressebeskyttelse = "UGRADERT")
        val oppdatertPerson = personopplysningerOutput.readKeyValue()
        assertEquals("12345678910", oppdatertPerson.key)
        assertEquals(expectedPersonopplysninger, oppdatertPerson.value)
    }

    @Test
    fun `kaller norg etter at pdl er joinet`() = testApp { mocks ->
        val skjermingInput = mocks.kafka.inputTopic(Topics.skjerming)
        val personopplysningerInput = mocks.kafka.inputTopic(Topics.personopplysninger)
        val personopplysningerOutput = mocks.kafka.outputTopic(Topics.personopplysninger)

        skjermingInput.pipeInput("12345678910", SkjermetDto(LocalDateTime.now().minusDays(30), null))
        personopplysningerInput.pipeInput("12345678910", PersonopplysningerDto())

        val producedRecords = personopplysningerOutput.readKeyValuesToList()
        val expectedSkjerming = SkjermingDto(true, LocalDate.now().minusDays(30), null)
        val expected = PersonopplysningerDto(
            skjerming = expectedSkjerming,
            adressebeskyttelse = "UGRADERT",
            geografiskTilknytning = "4632",
            norgEnhetId = "4201",
        )
        assertEquals(3, producedRecords.size)
        assertEquals("12345678910", producedRecords.last().key)
        assertEquals(expected, producedRecords.last().value)
    }
}

private fun testApp(block: ApplicationTestBuilder.(mocks: Mocks) -> Unit) = Mocks().use { mocks ->
    val externalProperties = mapOf(
        "AZURE_OPENID_CONFIG_TOKEN_ENDPOINT" to "http://localhost:${mocks.oauth.port}/token",
        "AZURE_APP_CLIENT_ID" to "test",
        "AZURE_APP_CLIENT_SECRET" to "test",
        "PROXY_BASEURL" to "http://localhost:${mocks.norg.port}",
        "PDL_URL" to "http://localhost:${mocks.pdl.port}/graphql",
        "PDL_SCOPE" to "test",
        "KAFKA_STREAMS_APPLICATION_ID" to "personopplysninger",
        "KAFKA_BROKERS" to "mock://kafka",
        "KAFKA_TRUSTSTORE_PATH" to "",
        "KAFKA_SECURITY_ENABLED" to "false",
        "KAFKA_KEYSTORE_PATH" to "",
        "KAFKA_CREDSTORE_PASSWORD" to "",
        "KAFKA_CLIENT_ID" to "personopplysninger",
    )
    EnvironmentVariables(externalProperties).execute {
        testApplication {
            application {
                personopplysninger(mocks.kafka)
                block(mocks)
            }
        }
    }
}
