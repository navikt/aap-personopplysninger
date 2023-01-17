package personopplysninger

import org.junit.jupiter.api.Test
import personopplysninger.kafka.Topics
import personopplysninger.streams.SøknadDto

internal class SøknadStreamTest {

    @Test
    fun `søknad produserer til personopplysninger 4 ganger`() = testApp { mocks ->
        val søknadTopic = mocks.kafka.testTopic(Topics.søknad)
        val personopplysningerTopic = mocks.kafka.testTopic(Topics.personopplysninger)

        søknadTopic.produce("123", ::SøknadDto)

        // 1st initiell personopplysning
        // 2nd skjermet personopplysning
        // 3nd pdl personopplysning
        // 4nd norg personopplysning
        personopplysningerTopic
            .assertThat()
            .hasNumberOfRecordsForKey("123", 4)
    }
}
