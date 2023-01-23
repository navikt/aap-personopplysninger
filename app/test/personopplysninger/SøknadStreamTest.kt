package personopplysninger

import org.junit.jupiter.api.Test
import personopplysninger.kafka.Topics

internal class SøknadStreamTest {

    @Test
    fun `søknad produserer til personopplysninger 4 ganger`() = testApp { mocks ->
        val søknadTopic = mocks.kafka.testTopic(Topics.søknad)
        val personopplysninger = mocks.kafka.testTopic(Topics.personopplysninger)
        val personopplysningerIntern = mocks.kafka.testTopic(Topics.personopplysningerIntern)

        søknadTopic.produce("123") { "".toByteArray() }

        // 1st initiell personopplysning
        // 2nd skjermet personopplysning
        // 3nd pdl personopplysning
        // 4nd norg personopplysning
        personopplysningerIntern
            .assertThat()
            .hasNumberOfRecordsForKey("123", 4)

        personopplysninger
            .assertThat()
            .hasNumberOfRecordsForKey("123", 1)
    }
}
