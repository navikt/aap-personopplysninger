package no.nav.aap.kafka.streams

import org.apache.kafka.streams.kstream.Joined
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.KeyValueMapper

fun <K, V, VO, VR> KStream<K, V>.join(joined: Joined<K, V, VO>, table: KTable<K, VO>, joiner: (V, VO) -> VR) =
    join(table, joiner, joined)!!

fun <K, V, KR> KStream<K, V>.selectKey(name: String, mapper: KeyValueMapper<in K, in V, out KR>) =
    selectKey(mapper, named(name))!!
