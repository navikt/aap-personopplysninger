package personopplysninger

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import personopplysninger.aktor.AktorDto
import personopplysninger.aktor.IdentifikatorDto
import personopplysninger.aktor.TypeDto
import personopplysninger.kafka.Tables
import personopplysninger.kafka.Topics

class IdentHendelseStreamTest {

    @Test
    fun `når en søker bytter personident oppdateres søker`() = testApp { mocks ->
        val aktørV2 = mocks.kafka.testTopic(Topics.aktørV2)
        val søkere = mocks.kafka.testTopic(Topics.søkere)

        søkere.produce("123") { "hello".toByteArray() }

        søkere.assertThat()
            .hasNumberOfRecordsForKey("123", 0)
            .hasNumberOfRecordsForKey("456", 0)

        val store = mocks.kafka.getStore<ByteArray>(Tables.søkere.stateStoreName)
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

        søkere.assertThat()
            .hasNumberOfRecordsForKey("123", 1)
            .hasValuesForPredicate("123", 1) { it ==  null }
            .hasNumberOfRecordsForKey("456", 1)
            .hasValueMatching("456") {
                "hello".toByteArray()
            }
    }
}
