package nom.skjerming.kafka

import nom.skjerming.skjerming.SkjermetPersonDto
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Joined
import org.apache.kafka.streams.kstream.Produced

data class Topic<K, V>(
    val name: String,
    val keySerde: Serde<K>,
    val valueSerde: Serde<V>,
) {
    fun consumed(named: String): Consumed<K, V> = Consumed.with(keySerde, valueSerde).withName(named)
    fun produced(named: String): Produced<K, V> = Produced.with(keySerde, valueSerde).withName(named)

    fun <R : Any> joined(right: Topic<K, R>): Joined<K, V, R> =
        Joined.with(keySerde, valueSerde, right.valueSerde, "$name-joined-${right.name}")
}

object Topics {
    val skjerming = Topic("nom.skjermede-personer-v1", Serdes.StringSerde(), JsonSerde.create<SkjermetPersonDto>())
}

