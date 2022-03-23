package no.nav.aap.kafka.streams

data class Table<K, V>(
    val name: String,
    val source: Topic<K, V>,
) {
    val stateStoreName = "$name-state-store"
}
