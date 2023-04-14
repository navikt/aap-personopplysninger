package personopplysninger.streams

import no.nav.aap.kafka.streams.v2.Topology
import personopplysninger.domain.PersonopplysningerInternDto
import personopplysninger.kafka.Topics

internal fun Topology.søknadStream() {
    consume(Topics.søknad)
        .map { _, _ -> PersonopplysningerInternDto() }
        .produce(Topics.personopplysningerIntern)
}
