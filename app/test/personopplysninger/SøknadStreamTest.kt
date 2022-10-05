package personopplysninger

import no.nav.aap.kafka.streams.test.readAndAssert
import org.junit.jupiter.api.Test
import personopplysninger.kafka.Topics
import personopplysninger.streams.SøknadDto

internal class SøknadStreamTest {

    @Test
    fun `søknad produserer til personopplysninger 4 ganger`() = testApp { mocks ->
        val søknadInput = mocks.kafka.inputTopic(Topics.søknad)
        val personopplysningerOutput = mocks.kafka.outputTopic(Topics.personopplysninger)

        søknadInput.pipeInput("123", SøknadDto())

        // 1st initiell personopplysning
        // 2nd skjermet personopplysning
        // 3nd pdl personopplysning
        // 4nd norg personopplysning
        personopplysningerOutput
            .readAndAssert()
            .hasNumberOfRecordsForKey("123", 4)
    }
}
