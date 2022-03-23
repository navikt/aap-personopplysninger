package no.nav.aap.kafka

data class Table<K, V>(
    val name: String,
    val source: Topic<K, V>,
) {
    val stateStoreName = "$name-state-store"
}
