package personopplysninger

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import personopplysninger.aktor.AktorDto
import personopplysninger.aktor.IdentifikatorDto
import personopplysninger.aktor.TypeDto
import personopplysninger.kafka.Tables
import personopplysninger.kafka.Topics

class AktørStreamTest {

    @Test
    fun `når en søker bytter personident oppdateres søker`() = testApp { mocks ->
        val aktørV2 = mocks.kafka.testTopic(Topics.aktørV2)
        val søkere = mocks.kafka.testTopic(Topics.søkere)
        val endredePersonidenter = mocks.kafka.testTopic(Topics.endredePersonidenter)

        søkere.produce("123") { "hello".toByteArray() }


        val store = mocks.kafka.getStore(Tables.søkere)
        assertNotNull(store["123"])
        assertNull(store["456"])

        aktørV2.produce("ABC") {
            AktorDto(
                listOf(
                    IdentifikatorDto("123", TypeDto.FOLKEREGISTERIDENT, false),
                    IdentifikatorDto("456", TypeDto.FOLKEREGISTERIDENT, true),
                    IdentifikatorDto("1234567890123", TypeDto.AKTORID, false),
                )
            )
        }
        endredePersonidenter.assertThat()
            .hasValuesForPredicate("123"){ it == "456" }
            .hasNumberOfRecords(1)
    }
}
