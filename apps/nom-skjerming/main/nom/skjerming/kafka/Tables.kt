package nom.skjerming.kafka

data class Table<K, V>(
    val name: String,
    val source: Topic<K, V>,
) {
    val stateStoreName = "$name-state-store"
}

object Tables {
    val skjerming = Table("skjerming", Topics.skjerming)
}
