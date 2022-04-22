package personopplysninger

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import personopplysninger.Personopplysninger.PersonopplysningerDto
import personopplysninger.Personopplysninger.SkjermingDto
import no.nav.aap.kafka.streams.test.KStreamsMock
import no.nav.aap.kafka.streams.test.readAndAssert
import org.junit.Test
import personopplysninger.Mocks.Companion.port
import personopplysninger.mocks.*
import personopplysninger.skjerming.SkjermetDto
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import java.time.LocalDate
import java.time.LocalDateTime

class PersonopplysningerTest {

    @Test
    fun `personopplysninger joines i rekkefølge - skjerming, pdl, norg`() = testApp { mocks ->
        val skjermingInput = mocks.kafka.inputTopic(Topics.skjerming)
        val personopplysningerInput = mocks.kafka.inputTopic(Topics.personopplysninger)
        val personopplysningerOutput = mocks.kafka.outputTopic(Topics.personopplysninger)

        skjermingInput.pipeInput(KOMMUNE_PERSON, SkjermetDto(LocalDateTime.now().minusDays(30), null))
        personopplysningerInput.pipeInput(KOMMUNE_PERSON, PersonopplysningerDto())

        personopplysningerOutput.readAndAssert()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(KOMMUNE_PERSON, 3)
            .hasValueEquals(KOMMUNE_PERSON, 0) { skjermet }
            .hasValueEquals(KOMMUNE_PERSON, 1) { skjermet + gtKommune + ugradert }
            .hasValueEquals(KOMMUNE_PERSON, 2) { skjermet + gtKommune + ugradert + enhet }
    }

    @Test
    fun `person knyttes til bydel`() = testApp { mocks ->
        val personopplysningerInput = mocks.kafka.inputTopic(Topics.personopplysninger)
        val personopplysningerOutput = mocks.kafka.outputTopic(Topics.personopplysninger)

        personopplysningerInput.pipeInput(BYDEL_PERSON, PersonopplysningerDto())

        personopplysningerOutput.readAndAssert()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(BYDEL_PERSON, 3)
            .hasValueEquals(BYDEL_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(BYDEL_PERSON, 1) { ikkeSkjermet + gtBydel + ugradert }
            .hasValueEquals(BYDEL_PERSON, 2) { ikkeSkjermet + gtBydel + ugradert + enhet }
    }

    @Test
    fun `person knyttes til land`() = testApp { mocks ->
        val personopplysningerInput = mocks.kafka.inputTopic(Topics.personopplysninger)
        val personopplysningerOutput = mocks.kafka.outputTopic(Topics.personopplysninger)

        personopplysningerInput.pipeInput(SVENSK_PERSON, PersonopplysningerDto())

        personopplysningerOutput.readAndAssert()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(SVENSK_PERSON, 3)
            .hasValueEquals(SVENSK_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(SVENSK_PERSON, 1) { ikkeSkjermet + gtLand + ugradert }
            .hasValueEquals(SVENSK_PERSON, 2) { ikkeSkjermet + gtLand + ugradert + enhet }
    }

    @Test
    fun `person uten adressebeskyttelse`() = testApp { mocks ->
        val personopplysningerInput = mocks.kafka.inputTopic(Topics.personopplysninger)
        val personopplysningerOutput = mocks.kafka.outputTopic(Topics.personopplysninger)

        personopplysningerInput.pipeInput(PERSON_UTEN_GRADERING, PersonopplysningerDto())

        personopplysningerOutput.readAndAssert()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(PERSON_UTEN_GRADERING, 3)
            .hasValueEquals(PERSON_UTEN_GRADERING, 0) { ikkeSkjermet }
            .hasValueEquals(PERSON_UTEN_GRADERING, 1) { ikkeSkjermet + gtKommune + ugradert }
            .hasValueEquals(PERSON_UTEN_GRADERING, 2) { ikkeSkjermet + gtKommune + ugradert + enhet }
    }

    @Test
    fun `ugradert person`() = testApp { mocks ->
        val personopplysningerInput = mocks.kafka.inputTopic(Topics.personopplysninger)
        val personopplysningerOutput = mocks.kafka.outputTopic(Topics.personopplysninger)

        personopplysningerInput.pipeInput(UGRADERT_PERSON, PersonopplysningerDto())

        personopplysningerOutput.readAndAssert()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(UGRADERT_PERSON, 3)
            .hasValueEquals(UGRADERT_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(UGRADERT_PERSON, 1) { ikkeSkjermet + gtKommune + ugradert }
            .hasValueEquals(UGRADERT_PERSON, 2) { ikkeSkjermet + gtKommune + ugradert + enhet }
    }

    @Test
    fun `fortrolig person`() = testApp { mocks ->
        val personopplysningerInput = mocks.kafka.inputTopic(Topics.personopplysninger)
        val personopplysningerOutput = mocks.kafka.outputTopic(Topics.personopplysninger)

        personopplysningerInput.pipeInput(FORTROLIG_PERSON, PersonopplysningerDto())

        personopplysningerOutput.readAndAssert()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(FORTROLIG_PERSON, 3)
            .hasValueEquals(FORTROLIG_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(FORTROLIG_PERSON, 1) { ikkeSkjermet + gtKommune + fortrolig }
            .hasValueEquals(FORTROLIG_PERSON, 2) { ikkeSkjermet + gtKommune + fortrolig + enhet }
    }

    @Test
    fun `strengt fortrolig person`() = testApp { mocks ->
        val personopplysningerInput = mocks.kafka.inputTopic(Topics.personopplysninger)
        val personopplysningerOutput = mocks.kafka.outputTopic(Topics.personopplysninger)

        personopplysningerInput.pipeInput(STRENGT_FORTROLIG_PERSON, PersonopplysningerDto())

        personopplysningerOutput.readAndAssert()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(STRENGT_FORTROLIG_PERSON, 3)
            .hasValueEquals(STRENGT_FORTROLIG_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(STRENGT_FORTROLIG_PERSON, 1) { ikkeSkjermet + gtKommune + strengtFortrolig }
            .hasValueEquals(STRENGT_FORTROLIG_PERSON, 2) { ikkeSkjermet + gtKommune + strengtFortrolig + enhet }
    }

    @Test
    fun `strengt fortrolig person utland`() = testApp { mocks ->
        val personopplysningerInput = mocks.kafka.inputTopic(Topics.personopplysninger)
        val personopplysningerOutput = mocks.kafka.outputTopic(Topics.personopplysninger)

        personopplysningerInput.pipeInput(STRENGT_FORTROLIG_UTLAND_PERSON, PersonopplysningerDto())

        personopplysningerOutput.readAndAssert()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(STRENGT_FORTROLIG_UTLAND_PERSON, 3)
            .hasValueEquals(STRENGT_FORTROLIG_UTLAND_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(STRENGT_FORTROLIG_UTLAND_PERSON, 1) { ikkeSkjermet + gtKommune + strengtFortroligUtland }
            .hasValueEquals(STRENGT_FORTROLIG_UTLAND_PERSON, 2) {
                ikkeSkjermet + gtKommune + strengtFortroligUtland + enhet
            }
    }
}

private operator fun PersonopplysningerDto.plus(other: PersonopplysningerDto) = copy(
    skjerming = skjerming ?: other.skjerming,
    geografiskTilknytning = geografiskTilknytning ?: other.geografiskTilknytning,
    adressebeskyttelse = adressebeskyttelse ?: other.adressebeskyttelse,
    norgEnhetId = norgEnhetId ?: other.norgEnhetId
)

private val skjermet = PersonopplysningerDto(skjerming = SkjermingDto(true, LocalDate.now().minusDays(30), null))
private val ikkeSkjermet = PersonopplysningerDto(skjerming = SkjermingDto(false, null, null))
private val gtKommune = PersonopplysningerDto(geografiskTilknytning = "0301")
private val gtBydel = PersonopplysningerDto(geografiskTilknytning = "030101")
private val gtLand = PersonopplysningerDto(geografiskTilknytning = "SWE")
private val ugradert = PersonopplysningerDto(adressebeskyttelse = "UGRADERT")
private val fortrolig = PersonopplysningerDto(adressebeskyttelse = "FORTROLIG")
private val strengtFortrolig = PersonopplysningerDto(adressebeskyttelse = "STRENGT_FORTROLIG")
private val strengtFortroligUtland = PersonopplysningerDto(adressebeskyttelse = "STRENGT_FORTROLIG_UTLAND")
private val enhet = PersonopplysningerDto(norgEnhetId = "4201")

private fun testApp(test: suspend ApplicationTestBuilder.(mocks: Mocks) -> Unit) = Mocks().use { mocks ->
    EnvironmentVariables(containerProperties(mocks)).execute {
        testApplication {
            application {
                personopplysninger(mocks.kafka)
                runBlocking { this@testApplication.test(mocks) }
            }
        }
    }
}

private class Mocks : AutoCloseable {
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
}

/** Mock the external properties used from within a nais pod */
private fun containerProperties(mocks: Mocks) = mapOf(
    "AZURE_OPENID_CONFIG_TOKEN_ENDPOINT" to "http://localhost:${mocks.oauth.port}/token",
    "AZURE_APP_CLIENT_ID" to "test",
    "AZURE_APP_CLIENT_SECRET" to "test",
    "PROXY_BASEURL" to "http://localhost:${mocks.norg.port}",
    "PDL_URL" to "http://localhost:${mocks.pdl.port}/graphql",
    "PDL_SCOPE" to "test",
    "KAFKA_STREAMS_APPLICATION_ID" to "personopplysninger",
    "KAFKA_BROKERS" to "mock://kafka",
    "KAFKA_TRUSTSTORE_PATH" to "",
    "KAFKA_KEYSTORE_PATH" to "",
    "KAFKA_CREDSTORE_PASSWORD" to "",
    "KAFKA_CLIENT_ID" to "personopplysninger",
)
