package personopplysninger

import org.junit.jupiter.api.Test
import personopplysninger.PersonopplysningerIntern.enhet
import personopplysninger.PersonopplysningerIntern.fortrolig
import personopplysninger.PersonopplysningerIntern.gtBydel
import personopplysninger.PersonopplysningerIntern.gtKommune
import personopplysninger.PersonopplysningerIntern.gtLand
import personopplysninger.PersonopplysningerIntern.ikkeSkjermet
import personopplysninger.PersonopplysningerIntern.navn
import personopplysninger.PersonopplysningerIntern.skjermet
import personopplysninger.PersonopplysningerIntern.strengtFortrolig
import personopplysninger.PersonopplysningerIntern.strengtFortroligUtland
import personopplysninger.PersonopplysningerIntern.ugradert
import personopplysninger.domain.NavnDto
import personopplysninger.domain.PersonopplysningerDto
import personopplysninger.domain.PersonopplysningerInternDto
import personopplysninger.domain.SkjermingDto
import personopplysninger.kafka.Topics
import personopplysninger.mocks.BYDEL_PERSON
import personopplysninger.mocks.FORTROLIG_PERSON
import personopplysninger.mocks.KOMMUNE_PERSON
import personopplysninger.mocks.PERSON_UTEN_GRADERING
import personopplysninger.mocks.STRENGT_FORTROLIG_PERSON
import personopplysninger.mocks.STRENGT_FORTROLIG_UTLAND_PERSON
import personopplysninger.mocks.SVENSK_PERSON
import personopplysninger.mocks.UGRADERT_PERSON
import personopplysninger.streams.SkjermetDto
import java.time.LocalDate
import java.time.LocalDateTime

class PersonopplysningerTest {

    @Test
    fun `personopplysninger joines i rekkefølge - skjerming, pdl, norg`() = testApp { mocks ->
        val skjermingInput = mocks.kafka.testTopic(Topics.skjerming)
        val søknadInput = mocks.kafka.testTopic(Topics.søknad)
        val personopplysningerIntern = mocks.kafka.testTopic(Topics.personopplysningerIntern)
        val personopplysninger = mocks.kafka.testTopic(Topics.personopplysninger)

        skjermingInput.produce(KOMMUNE_PERSON) { SkjermetDto(LocalDateTime.now().minusDays(30), null) }
        søknadInput.produce(KOMMUNE_PERSON) { "".toByteArray() }

        personopplysningerIntern.assertThat()
            .hasNumberOfRecordsForKey(KOMMUNE_PERSON, 4)
            .hasValueEquals(KOMMUNE_PERSON, 0) { PersonopplysningerInternDto() }
            .hasValueEquals(KOMMUNE_PERSON, 1) { skjermet }
            .hasValueEquals(KOMMUNE_PERSON, 2) { skjermet + gtKommune + ugradert + navn }
            .hasValueEquals(KOMMUNE_PERSON, 3) { skjermet + gtKommune + ugradert + navn + enhet }
        personopplysninger.assertThat()
            .hasNumberOfRecordsForKey(KOMMUNE_PERSON, 1)
            .hasValueEquals(KOMMUNE_PERSON, 0) { PersonopplysningerKomplett.komplett1 }
    }

    @Test
    fun `person knyttes til bydel`() = testApp { mocks ->
        val personopplysningerIntern = mocks.kafka.testTopic(Topics.personopplysningerIntern)
        val personopplysninger = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerIntern.produce(BYDEL_PERSON) { PersonopplysningerInternDto() }

        personopplysningerIntern.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(BYDEL_PERSON, 3)
            .hasValueEquals(BYDEL_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(BYDEL_PERSON, 1) { ikkeSkjermet + gtBydel + ugradert + navn }
            .hasValueEquals(BYDEL_PERSON, 2) { ikkeSkjermet + gtBydel + ugradert + navn + enhet }
        personopplysninger.assertThat()
            .hasNumberOfRecordsForKey(BYDEL_PERSON, 1)
            .hasValueEquals(BYDEL_PERSON, 0) { PersonopplysningerKomplett.komplett2 }
    }

    @Test
    fun `person knyttes til land`() = testApp { mocks ->
        val personopplysningerIntern = mocks.kafka.testTopic(Topics.personopplysningerIntern)
        val personopplysninger = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerIntern.produce(SVENSK_PERSON) { PersonopplysningerInternDto() }

        personopplysningerIntern.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(SVENSK_PERSON, 3)
            .hasValueEquals(SVENSK_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(SVENSK_PERSON, 1) { ikkeSkjermet + gtLand + ugradert + navn }
            .hasValueEquals(SVENSK_PERSON, 2) { ikkeSkjermet + gtLand + ugradert + navn + enhet }
        personopplysninger.assertThat()
            .hasNumberOfRecordsForKey(SVENSK_PERSON, 1)
            .hasValueEquals(SVENSK_PERSON, 0) { PersonopplysningerKomplett.komplett3 }
    }

    @Test
    fun `person uten adressebeskyttelse`() = testApp { mocks ->
        val personollysningerIntern = mocks.kafka.testTopic(Topics.personopplysningerIntern)
        val personopplysninger = mocks.kafka.testTopic(Topics.personopplysninger)

        personollysningerIntern.produce(PERSON_UTEN_GRADERING) { PersonopplysningerInternDto() }

        personollysningerIntern.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(PERSON_UTEN_GRADERING, 3)
            .hasValueEquals(PERSON_UTEN_GRADERING, 0) { ikkeSkjermet }
            .hasValueEquals(PERSON_UTEN_GRADERING, 1) { ikkeSkjermet + gtKommune + ugradert + navn }
            .hasValueEquals(PERSON_UTEN_GRADERING, 2) { ikkeSkjermet + gtKommune + ugradert + navn + enhet }
        personopplysninger.assertThat()
            .hasNumberOfRecordsForKey(PERSON_UTEN_GRADERING, 1)
            .hasValueEquals(PERSON_UTEN_GRADERING, 0) { PersonopplysningerKomplett.komplett4 }
    }

    @Test
    fun `ugradert person`() = testApp { mocks ->
        val personopplysningerIntern = mocks.kafka.testTopic(Topics.personopplysningerIntern)
        val personopplysninger = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerIntern.produce(UGRADERT_PERSON) { PersonopplysningerInternDto() }

        personopplysningerIntern.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(UGRADERT_PERSON, 3)
            .hasValueEquals(UGRADERT_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(UGRADERT_PERSON, 1) { ikkeSkjermet + gtKommune + ugradert + navn }
            .hasValueEquals(UGRADERT_PERSON, 2) { ikkeSkjermet + gtKommune + ugradert + navn + enhet }
        personopplysninger.assertThat()
            .hasNumberOfRecordsForKey(UGRADERT_PERSON, 1)
            .hasValueEquals(UGRADERT_PERSON, 0) { PersonopplysningerKomplett.komplett4 }
    }

    @Test
    fun `fortrolig person`() = testApp { mocks ->
        val personopplysningerIntern = mocks.kafka.testTopic(Topics.personopplysningerIntern)
        val personopplysninger = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerIntern.produce(FORTROLIG_PERSON) { PersonopplysningerInternDto() }

        personopplysningerIntern.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(FORTROLIG_PERSON, 3)
            .hasValueEquals(FORTROLIG_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(FORTROLIG_PERSON, 1) { ikkeSkjermet + gtKommune + fortrolig + navn }
            .hasValueEquals(FORTROLIG_PERSON, 2) { ikkeSkjermet + gtKommune + fortrolig + navn + enhet }
        personopplysninger.assertThat()
            .hasNumberOfRecordsForKey(FORTROLIG_PERSON, 1)
            .hasValueEquals(FORTROLIG_PERSON, 0) { PersonopplysningerKomplett.komplett5 }
    }

    @Test
    fun `strengt fortrolig person`() = testApp { mocks ->
        val personopplysningerIntern = mocks.kafka.testTopic(Topics.personopplysningerIntern)
        val personopplysninger = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerIntern.produce(STRENGT_FORTROLIG_PERSON) { PersonopplysningerInternDto() }

        personopplysningerIntern.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(STRENGT_FORTROLIG_PERSON, 3)
            .hasValueEquals(STRENGT_FORTROLIG_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(STRENGT_FORTROLIG_PERSON, 1) { ikkeSkjermet + gtKommune + strengtFortrolig + navn }
            .hasValueEquals(STRENGT_FORTROLIG_PERSON, 2) { ikkeSkjermet + gtKommune + strengtFortrolig + navn + enhet }
        personopplysninger.assertThat()
            .hasNumberOfRecordsForKey(STRENGT_FORTROLIG_PERSON, 1)
            .hasValueEquals(STRENGT_FORTROLIG_PERSON, 0) { PersonopplysningerKomplett.komplett6 }
    }

    @Test
    fun `strengt fortrolig person utland`() = testApp { mocks ->
        val personopplysningerIntern = mocks.kafka.testTopic(Topics.personopplysningerIntern)
        val personopplysninger = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerIntern.produce(STRENGT_FORTROLIG_UTLAND_PERSON) { PersonopplysningerInternDto() }

        personopplysningerIntern.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(STRENGT_FORTROLIG_UTLAND_PERSON, 3)
            .hasValueEquals(STRENGT_FORTROLIG_UTLAND_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(
                STRENGT_FORTROLIG_UTLAND_PERSON,
                1
            ) { ikkeSkjermet + gtKommune + strengtFortroligUtland + navn }
            .hasValueEquals(
                STRENGT_FORTROLIG_UTLAND_PERSON,
                2
            ) { ikkeSkjermet + gtKommune + strengtFortroligUtland + navn + enhet }
        personopplysninger.assertThat()
            .hasNumberOfRecordsForKey(STRENGT_FORTROLIG_UTLAND_PERSON, 1)
            .hasValueEquals(STRENGT_FORTROLIG_UTLAND_PERSON, 0) { PersonopplysningerKomplett.komplett7 }
    }
}

private operator fun PersonopplysningerInternDto.plus(other: PersonopplysningerInternDto) = copy(
    skjerming = skjerming ?: other.skjerming,
    geografiskTilknytning = geografiskTilknytning ?: other.geografiskTilknytning,
    adressebeskyttelse = adressebeskyttelse ?: other.adressebeskyttelse,
    norgEnhetId = norgEnhetId ?: other.norgEnhetId,
    navn = navn ?: other.navn
)

private object PersonopplysningerKomplett {
    // skjermet + gtKommune + ugradert + navn + enhet
    val komplett1 = PersonopplysningerDto(
        skjerming = SkjermingDto(true, LocalDate.now().minusDays(30), null),
        norgEnhetId = "4201",
        adressebeskyttelse = "UGRADERT",
        navn = NavnDto("Ola", null, "Normann"),
        geografiskTilknytning = "0301"
    )

    // ikkeSkjermet + gtBydel + ugradert + navn + enhet
    val komplett2 = PersonopplysningerDto(
        skjerming = SkjermingDto(false, null, null),
        norgEnhetId = "4201",
        adressebeskyttelse = "UGRADERT",
        navn = NavnDto("Ola", null, "Normann"),
        geografiskTilknytning = "030101"
    )

    // ikkeSkjermet + gtLand + ugradert + navn + enhet
    val komplett3 = PersonopplysningerDto(
        skjerming = SkjermingDto(false, null, null),
        norgEnhetId = "4201",
        adressebeskyttelse = "UGRADERT",
        navn = NavnDto("Ola", null, "Normann"),
        geografiskTilknytning = "SWE"
    )

    // ikkeSkjermet + gtKommune + ugradert + navn + enhet
    val komplett4 = PersonopplysningerDto(
        skjerming = SkjermingDto(false, null, null),
        norgEnhetId = "4201",
        adressebeskyttelse = "UGRADERT",
        navn = NavnDto("Ola", null, "Normann"),
        geografiskTilknytning = "0301"
    )

    // ikkeSkjermet + gtKommune + fortrolig + navn + enhet
    val komplett5 = PersonopplysningerDto(
        skjerming = SkjermingDto(false, null, null),
        norgEnhetId = "4201",
        adressebeskyttelse = "FORTROLIG",
        navn = NavnDto("Ola", null, "Normann"),
        geografiskTilknytning = "0301"
    )

    // ikkeSkjermet + gtKommune + strengtFortrolig + navn + enhet
    val komplett6 = PersonopplysningerDto(
        skjerming = SkjermingDto(false, null, null),
        norgEnhetId = "4201",
        adressebeskyttelse = "STRENGT_FORTROLIG",
        navn = NavnDto("Ola", null, "Normann"),
        geografiskTilknytning = "0301"
    )

    // ikkeSkjermet + gtKommune + strengtFortroligUtland + navn + enhet
    val komplett7 = PersonopplysningerDto(
        skjerming = SkjermingDto(false, null, null),
        norgEnhetId = "4201",
        adressebeskyttelse = "STRENGT_FORTROLIG_UTLAND",
        navn = NavnDto("Ola", null, "Normann"),
        geografiskTilknytning = "0301"
    )

}

private object PersonopplysningerIntern {
    val skjermet = PersonopplysningerInternDto(skjerming = SkjermingDto(true, LocalDate.now().minusDays(30), null))
    val ikkeSkjermet = PersonopplysningerInternDto(skjerming = SkjermingDto(false, null, null))
    val gtKommune = PersonopplysningerInternDto(geografiskTilknytning = "0301")
    val gtBydel = PersonopplysningerInternDto(geografiskTilknytning = "030101")
    val gtLand = PersonopplysningerInternDto(geografiskTilknytning = "SWE")
    val ugradert = PersonopplysningerInternDto(adressebeskyttelse = "UGRADERT")
    val fortrolig = PersonopplysningerInternDto(adressebeskyttelse = "FORTROLIG")
    val strengtFortrolig = PersonopplysningerInternDto(adressebeskyttelse = "STRENGT_FORTROLIG")
    val strengtFortroligUtland = PersonopplysningerInternDto(adressebeskyttelse = "STRENGT_FORTROLIG_UTLAND")
    val enhet = PersonopplysningerInternDto(norgEnhetId = "4201")
    val navn = PersonopplysningerInternDto(navn = NavnDto("Ola", null, "Normann"))
}