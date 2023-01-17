package personopplysninger.streams

import no.nav.aap.kafka.streams.extension.consume
import no.nav.aap.kafka.streams.extension.filterNotNull
import no.nav.aap.kafka.streams.extension.produce
import org.apache.kafka.streams.StreamsBuilder
import personopplysninger.domain.PersonopplysningerDto
import personopplysninger.kafka.Topics
import java.time.LocalDate
import kotlin.random.Random

internal fun StreamsBuilder.søknadStream() {
    consume(Topics.søknad)
        .filterNotNull("filter-soknad-tombstones")
        .mapValues { _, _ -> PersonopplysningerDto() }
        .produce(Topics.personopplysninger, "initiated-personopplysninger", true)
}

// se https://github.com/navikt/aap-soknad-api/blob/1b4563e81b418be7e9a39863278b607f685b9cee/src/main/kotlin/no/nav/aap/api/s%C3%B8knad/model/StandardS%C3%B8knad.kt
data class SøknadDto(
    val fødselsdato: LocalDate = LocalDate.now().minusYears(Random.nextLong(0, 100))
)
