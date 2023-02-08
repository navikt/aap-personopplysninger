package personopplysninger.kafka

import no.nav.aap.kafka.streams.v2.Table


object Tables {
    val skjerming = Table(Topics.skjerming, stateStoreName = "skjerming-state-store-v2")
    val søkere = Table(Topics.søkere, stateStoreName = "sokere-state-store-v2")
}
