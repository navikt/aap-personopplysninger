package personopplysninger.streams

import no.nav.aap.kafka.streams.extension.consume
import no.nav.aap.kafka.streams.extension.filterNotNull
import no.nav.aap.kafka.streams.extension.produce
import org.apache.kafka.streams.StreamsBuilder
import personopplysninger.domain.PersonopplysningerDto
import personopplysninger.domain.PersonopplysningerInternDto
import personopplysninger.kafka.Topics
import java.time.LocalDate
import kotlin.random.Random

internal fun StreamsBuilder.søknadStream() {
    consume(Topics.søknad)
        .filterNotNull("filter-soknad-tombstones")
        .mapValues { _, _ -> PersonopplysningerInternDto() }
        .produce(Topics.personopplysningerIntern, "initiated-personopplysninger", true)
}
