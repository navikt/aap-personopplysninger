package personopplysninger.kafka

import no.nav.aap.kafka.streams.Table

object Tables {
    val skjerming = Table("skjerming", Topics.skjerming, stateStoreName = "skjerming-state-store-v2")
    val søkere = Table("identer", Topics.søkere);
}
