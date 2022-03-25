package no.nav.aap.kafka.streams

data class Table<V>(
    val name: String,
    val source: Topic<V>,
) {
    val stateStoreName = "$name-state-store"
}
