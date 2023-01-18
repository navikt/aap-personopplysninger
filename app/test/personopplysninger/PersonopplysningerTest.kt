package personopplysninger

import io.ktor.util.*
import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.avro.generic.GenericRecordBuilder
import org.apache.avro.reflect.ReflectData
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import personopplysninger.domain.NavnDto
import personopplysninger.domain.PersonopplysningerDto
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
import personopplysninger.streams.*
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

class PersonopplysningerTest {
    @Language("Json")
    val json = """ 
        {
    "type": "record",
    "name": "Aktor",
    "namespace": "no.nav.person.pdl.aktor.v2",
    "fields": [
      {
        "name": "identifikatorer",
        "type": {
          "type": "array",
          "items": {
            "type": "record",
            "name": "Identifikator",
            "fields": [
              {
                "name": "idnummer",
                "type": {
                  "type": "string",
                  "avro.java.string": "String"
                }
              },
              {
                "name": "type",
                "type": {
                  "type": "enum",
                  "name": "Type",
                  "symbols": [
                    "FOLKEREGISTERIDENT",
                    "AKTORID",
                    "NPID"
                  ]
                }
              },
              {
                "name": "gjeldende",
                "type": "boolean"
              }
            ]
          }
        }
      }
    ]
  }
    """.trimIndent()

    val listJson = """
        {
          "type": "array",
          "namespace": "no.nav.person.pdl.aktor.v2",
          "items": {
            "type": "record",
            "name": "Identifikator",
            "fields": [
              {
                "name": "idnummer",
                "type": {
                  "type": "string",
                  "avro.java.string": "String"
                }
              },
              {
                "name": "type",
                "type": {
                  "type": "enum",
                  "name": "Type",
                  "symbols": [
                    "FOLKEREGISTERIDENT",
                    "AKTORID",
                    "NPID"
                  ]
                }
              },
              {
                "name": "gjeldende",
                "type": "boolean"
              }
            ]
          }
        }
    """.trimIndent()

    @Language("Json")
    val identifikatorerSchemaJson = """
        {
    "type": "record",
    "name": "Identifikator",
    "namespace": "no.nav.person.pdl.aktor.v2",
    "fields": [
      {
        "name": "idnummer",
        "type": {
          "type": "string",
          "avro.java.string": "String"
        }
      },
      {
        "name": "type",
        "type": {
          "type": "enum",
          "name": "Type",
          "symbols": [
            "FOLKEREGISTERIDENT",
            "AKTORID",
            "NPID"
          ]
        }
      },
      {
        "name": "gjeldende",
        "type": "boolean"
      }
    ]
  }
    """.trimIndent()

    val enumSchemaJson = """
        {
    "type": "enum",
    "name": "Type",
    "namespace": "no.nav.person.pdl.aktor.v2",
    "symbols": [
      "FOLKEREGISTERIDENT",
      "AKTORID",
      "NPID"
    ]
  }
    """.trimIndent()

    @Test
    fun test() = testApp { mocks ->
        val identhendelse = mocks.kafka.testTopic(Topics.identHendelser)
        val test = mocks.kafka.testTopic(Topics.test)
        val schema = Schema.Parser().parse(json)
        val listSchema = Schema.Parser().parse(listJson)
        val enumSchema = Schema.Parser().parse(enumSchemaJson)
        val identifikatorerSchema = Schema.Parser().parse(identifikatorerSchemaJson)

        val identifikator = GenericData.Record(identifikatorerSchema).apply {
            put("idnummer", "1234")
            put("type", GenericData.EnumSymbol(enumSchema, "FOLKEREGISTERIDENT"))
            put("gjeldende", true)
        }
        val record = GenericData.Record(schema).apply {
            put("identifikatorer", GenericData.Array(listSchema, listOf(identifikator)))
        }
        identhendelse.produce("hei") { record }
        val result = test.readValue()
        assertEquals(Aktor(listOf(Identifikator("1234", Type.FOLKEREGISTERIDENT, true))), result)

    }


    @Test
    fun `personopplysninger joines i rekkefølge - skjerming, pdl, norg`() = testApp { mocks ->
        val skjermingInput = mocks.kafka.testTopic(Topics.skjerming)
        val søknadInput = mocks.kafka.testTopic(Topics.søknad)
        val personopplysningerOutput = mocks.kafka.testTopic(Topics.personopplysninger)

        skjermingInput.produce(KOMMUNE_PERSON) { SkjermetDto(LocalDateTime.now().minusDays(30), null) }
        søknadInput.produce(KOMMUNE_PERSON) { SøknadDto() }

        personopplysningerOutput.assertThat()
            .hasNumberOfRecordsForKey(KOMMUNE_PERSON, 4)
            .hasValueEquals(KOMMUNE_PERSON, 0) { PersonopplysningerDto() }
            .hasValueEquals(KOMMUNE_PERSON, 1) { skjermet }
            .hasValueEquals(KOMMUNE_PERSON, 2) { skjermet + gtKommune + ugradert + navn }
            .hasValueEquals(KOMMUNE_PERSON, 3) { skjermet + gtKommune + ugradert + navn + enhet }
    }

    @Test
    fun `person knyttes til bydel`() = testApp { mocks ->
        val personopplysninger = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysninger.produce(BYDEL_PERSON) { PersonopplysningerDto() }

        personopplysninger.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(BYDEL_PERSON, 3)
            .hasValueEquals(BYDEL_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(BYDEL_PERSON, 1) { ikkeSkjermet + gtBydel + ugradert + navn }
            .hasValueEquals(BYDEL_PERSON, 2) { ikkeSkjermet + gtBydel + ugradert + navn + enhet }
    }

    @Test
    fun `person knyttes til land`() = testApp { mocks ->
        val personopplysningerInput = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerInput.produce(SVENSK_PERSON) { PersonopplysningerDto() }

        personopplysningerInput.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(SVENSK_PERSON, 3)
            .hasValueEquals(SVENSK_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(SVENSK_PERSON, 1) { ikkeSkjermet + gtLand + ugradert + navn }
            .hasValueEquals(SVENSK_PERSON, 2) { ikkeSkjermet + gtLand + ugradert + navn + enhet }
    }

    @Test
    fun `person uten adressebeskyttelse`() = testApp { mocks ->
        val personopplysningerInput = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerInput.produce(PERSON_UTEN_GRADERING) { PersonopplysningerDto() }

        personopplysningerInput.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(PERSON_UTEN_GRADERING, 3)
            .hasValueEquals(PERSON_UTEN_GRADERING, 0) { ikkeSkjermet }
            .hasValueEquals(PERSON_UTEN_GRADERING, 1) { ikkeSkjermet + gtKommune + ugradert + navn }
            .hasValueEquals(PERSON_UTEN_GRADERING, 2) { ikkeSkjermet + gtKommune + ugradert + navn + enhet }
    }

    @Test
    fun `ugradert person`() = testApp { mocks ->
        val personopplysningerInput = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerInput.produce(UGRADERT_PERSON) { PersonopplysningerDto() }

        personopplysningerInput.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(UGRADERT_PERSON, 3)
            .hasValueEquals(UGRADERT_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(UGRADERT_PERSON, 1) { ikkeSkjermet + gtKommune + ugradert + navn }
            .hasValueEquals(UGRADERT_PERSON, 2) { ikkeSkjermet + gtKommune + ugradert + navn + enhet }
    }

    @Test
    fun `fortrolig person`() = testApp { mocks ->
        val personopplysningerInput = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerInput.produce(FORTROLIG_PERSON) { PersonopplysningerDto() }

        personopplysningerInput.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(FORTROLIG_PERSON, 3)
            .hasValueEquals(FORTROLIG_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(FORTROLIG_PERSON, 1) { ikkeSkjermet + gtKommune + fortrolig + navn }
            .hasValueEquals(FORTROLIG_PERSON, 2) { ikkeSkjermet + gtKommune + fortrolig + navn + enhet }
    }

    @Test
    fun `strengt fortrolig person`() = testApp { mocks ->
        val personopplysningerInput = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerInput.produce(STRENGT_FORTROLIG_PERSON) { PersonopplysningerDto() }

        personopplysningerInput.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(STRENGT_FORTROLIG_PERSON, 3)
            .hasValueEquals(STRENGT_FORTROLIG_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(STRENGT_FORTROLIG_PERSON, 1) { ikkeSkjermet + gtKommune + strengtFortrolig + navn }
            .hasValueEquals(STRENGT_FORTROLIG_PERSON, 2) { ikkeSkjermet + gtKommune + strengtFortrolig + navn + enhet }
    }

    @Test
    fun `strengt fortrolig person utland`() = testApp { mocks ->
        val personopplysningerInput = mocks.kafka.testTopic(Topics.personopplysninger)

        personopplysningerInput.produce(STRENGT_FORTROLIG_UTLAND_PERSON) { PersonopplysningerDto() }

        personopplysningerInput.assertThat()
            .hasNumberOfRecords(3)
            .hasNumberOfRecordsForKey(STRENGT_FORTROLIG_UTLAND_PERSON, 3)
            .hasValueEquals(STRENGT_FORTROLIG_UTLAND_PERSON, 0) { ikkeSkjermet }
            .hasValueEquals(
                STRENGT_FORTROLIG_UTLAND_PERSON,
                1
            ) { ikkeSkjermet + gtKommune + strengtFortroligUtland + navn }
            .hasValueEquals(STRENGT_FORTROLIG_UTLAND_PERSON, 2) {
                ikkeSkjermet + gtKommune + strengtFortroligUtland + navn + enhet
            }
    }
}

private operator fun PersonopplysningerDto.plus(other: PersonopplysningerDto) = copy(
    skjerming = skjerming ?: other.skjerming,
    geografiskTilknytning = geografiskTilknytning ?: other.geografiskTilknytning,
    adressebeskyttelse = adressebeskyttelse ?: other.adressebeskyttelse,
    norgEnhetId = norgEnhetId ?: other.norgEnhetId,
    navn = navn ?: other.navn
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
private val navn = PersonopplysningerDto(navn = NavnDto("Ola", null, "Normann"))
