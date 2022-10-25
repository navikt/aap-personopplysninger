package personopplysninger

import org.junit.jupiter.api.Test
import personopplysninger.Personopplysninger.PersonopplysningerDto
import personopplysninger.Personopplysninger.SkjermingDto
import personopplysninger.mocks.BYDEL_PERSON
import personopplysninger.mocks.FORTROLIG_PERSON
import personopplysninger.mocks.KOMMUNE_PERSON
import personopplysninger.mocks.PERSON_UTEN_GRADERING
import personopplysninger.mocks.STRENGT_FORTROLIG_PERSON
import personopplysninger.mocks.STRENGT_FORTROLIG_UTLAND_PERSON
import personopplysninger.mocks.SVENSK_PERSON
import personopplysninger.mocks.UGRADERT_PERSON
import personopplysninger.skjerming.SkjermetDto
import java.time.LocalDate
import java.time.LocalDateTime

class PersonopplysningerTest {

    @Test
    fun `personopplysninger joines i rekkefølge - skjerming, pdl, norg`() = testApp { mocks ->
        val skjermingTopic = mocks.kafka.testTopic(Topics.skjerming)
        val personopplysningerTopic = mocks.kafka.testTopic(Topics.personopplysninger)

        skjermingTopic.produce(KOMMUNE_PERSON) { SkjermetDto(LocalDateTime.now().minusDays(30), null) }
        personopplysningerTopic.produce(KOMMUNE_PERSON, ::PersonopplysningerDto)

        personopplysningerTopic.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(KOMMUNE_PERSON, 3)
            .hasValueEquals(KOMMUNE_PERSON, 0) { skjermet }
            .hasValueEquals(KOMMUNE_PERSON, 1) { skjermet + gtKommune + ugradert }
            .hasValueEquals(KOMMUNE_PERSON, 2) { skjermet + gtKommune + ugradert + enhet }
    }

    @Test
    fun `person knyttes til bydel`() = testApp { mocks ->
        val personopplysningerTopic = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerTopic.produce(BYDEL_PERSON, ::PersonopplysningerDto)

        personopplysningerTopic.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(BYDEL_PERSON, 3)
            .hasValueEquals(BYDEL_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(BYDEL_PERSON, 1) { ikkeSkjermet + gtBydel + ugradert }
            .hasValueEquals(BYDEL_PERSON, 2) { ikkeSkjermet + gtBydel + ugradert + enhet }
    }

    @Test
    fun `person knyttes til land`() = testApp { mocks ->
        val personopplysningerTopic = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerTopic.produce(SVENSK_PERSON, ::PersonopplysningerDto)

        personopplysningerTopic.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(SVENSK_PERSON, 3)
            .hasValueEquals(SVENSK_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(SVENSK_PERSON, 1) { ikkeSkjermet + gtLand + ugradert }
            .hasValueEquals(SVENSK_PERSON, 2) { ikkeSkjermet + gtLand + ugradert + enhet }
    }

    @Test
    fun `person uten adressebeskyttelse`() = testApp { mocks ->
        val personopplysningerTopic = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerTopic.produce(PERSON_UTEN_GRADERING, ::PersonopplysningerDto)

        personopplysningerTopic.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(PERSON_UTEN_GRADERING, 3)
            .hasValueEquals(PERSON_UTEN_GRADERING, 0) { ikkeSkjermet }
            .hasValueEquals(PERSON_UTEN_GRADERING, 1) { ikkeSkjermet + gtKommune + ugradert }
            .hasValueEquals(PERSON_UTEN_GRADERING, 2) { ikkeSkjermet + gtKommune + ugradert + enhet }
    }

    @Test
    fun `ugradert person`() = testApp { mocks ->
        val personopplysningerTopic = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerTopic.produce(UGRADERT_PERSON, ::PersonopplysningerDto)

        personopplysningerTopic.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(UGRADERT_PERSON, 3)
            .hasValueEquals(UGRADERT_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(UGRADERT_PERSON, 1) { ikkeSkjermet + gtKommune + ugradert }
            .hasValueEquals(UGRADERT_PERSON, 2) { ikkeSkjermet + gtKommune + ugradert + enhet }
    }

    @Test
    fun `fortrolig person`() = testApp { mocks ->
        val personopplysningerTopic = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerTopic.produce(FORTROLIG_PERSON, ::PersonopplysningerDto)

        personopplysningerTopic.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(FORTROLIG_PERSON, 3)
            .hasValueEquals(FORTROLIG_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(FORTROLIG_PERSON, 1) { ikkeSkjermet + gtKommune + fortrolig }
            .hasValueEquals(FORTROLIG_PERSON, 2) { ikkeSkjermet + gtKommune + fortrolig + enhet }
    }

    @Test
    fun `strengt fortrolig person`() = testApp { mocks ->
        val personopplysningerTopic = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerTopic.produce(STRENGT_FORTROLIG_PERSON, ::PersonopplysningerDto)

        personopplysningerTopic.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(STRENGT_FORTROLIG_PERSON, 3)
            .hasValueEquals(STRENGT_FORTROLIG_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(STRENGT_FORTROLIG_PERSON, 1) { ikkeSkjermet + gtKommune + strengtFortrolig }
            .hasValueEquals(STRENGT_FORTROLIG_PERSON, 2) { ikkeSkjermet + gtKommune + strengtFortrolig + enhet }
    }

    @Test
    fun `strengt fortrolig person utland`() = testApp { mocks ->
        val personopplysningerTopic = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerTopic.produce(STRENGT_FORTROLIG_UTLAND_PERSON, ::PersonopplysningerDto)

        personopplysningerTopic.assertThat()
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
