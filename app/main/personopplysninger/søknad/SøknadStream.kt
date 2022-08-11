package personopplysninger.søknad

import no.nav.aap.kafka.streams.extension.consume
import no.nav.aap.kafka.streams.extension.filterNotNull
import no.nav.aap.kafka.streams.extension.produce
import org.apache.kafka.streams.StreamsBuilder
import personopplysninger.Personopplysninger
import personopplysninger.Topics

internal fun StreamsBuilder.søknadStream() {
    consume(Topics.søknad)
        .filterNotNull("filter-soknad-tombstones")
        .mapValues { _, _ -> Personopplysninger.PersonopplysningerDto() }
        .produce(Topics.personopplysninger, "initiated-personopplysninger")
}
