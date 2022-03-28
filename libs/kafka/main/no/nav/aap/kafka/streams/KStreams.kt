package no.nav.aap.kafka.streams

import no.nav.aap.kafka.KafkaConfig
import no.nav.aap.kafka.ProcessingExceptionHandler
import no.nav.aap.kafka.plus
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.*
import org.apache.kafka.streams.KafkaStreams.State.*
import org.apache.kafka.streams.StoreQueryParameters.fromNameAndType
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.QueryableStoreTypes.keyValueStore
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.slf4j.LoggerFactory

typealias Store<V> = ReadOnlyKeyValueStore<String, V>

private val secureLog = LoggerFactory.getLogger("secureLog")

interface Kafka : AutoCloseable {
    fun start(kafkaConfig: KafkaConfig, streamsBuilder: StreamsBuilder.() -> Unit)
    fun started(): Boolean
    fun healthy(): Boolean
    fun <V> getStore(name: String): Store<V>
}

object KStreams : Kafka {
    private lateinit var streams: KafkaStreams
    private var started: Boolean = false

    override fun start(kafkaConfig: KafkaConfig, streamsBuilder: StreamsBuilder.() -> Unit) {
        val topology = StreamsBuilder().apply(streamsBuilder).build()
        streams = KafkaStreams(topology, kafkaConfig.consumer + kafkaConfig.producer).apply {
            setUncaughtExceptionHandler(ProcessingExceptionHandler)
            setStateListener { newState, _ -> if (newState == RUNNING) started = true }
            start()
        }
    }

    override fun <V> getStore(name: String): Store<V> = streams.store(fromNameAndType<Store<V>>(name, keyValueStore()))
    override fun started() = started
    override fun close() = streams.close()
    override fun healthy(): Boolean = streams.state() in listOf(CREATED, RUNNING, REBALANCING)
}

fun named(named: String): Named = Named.`as`(named)

fun <V> materialized(
    storeName: String,
    topic: Topic<V>,
    changelog: Boolean = true,
): Materialized<String, V, KeyValueStore<Bytes, ByteArray>> =
    Materialized.`as`<String, V, KeyValueStore<Bytes, ByteArray>>(storeName)
        .withKeySerde(topic.keySerde)
        .withValueSerde(topic.valueSerde)
        .apply { if (!changelog) withLoggingDisabled() }

fun <V> Store<V>.allValues(): List<V> = all().use { it.asSequence().map(KeyValue<String, V>::value).toList() }

fun <V> StreamsBuilder.consume(topic: Topic<V>, named: () -> String): KStream<String, V> =
    stream(topic.name, topic.consumed(named()))
        .peek { key, value -> secureLog.info("consumed [${topic.name}] K:$key V:$value") }

fun <V> KStream<String, V>.produce(topic: Topic<V>, named: () -> String) =
    peek { key, value -> secureLog.info("produced [${topic.name}] K:$key V:$value") }
        .to(topic.name, topic.produced(named()))

fun <V> KStream<String, V>.produce(table: Table<V>, changelog: Boolean = true, named: () -> String): KTable<String, V> =
    peek { key, value -> secureLog.info("produced [${table.stateStoreName}] K:$key V:$value") }
        .toTable(named(named()), materialized(table.stateStoreName, table.source, changelog))

@Suppress("UNCHECKED_CAST")
fun <V> KStream<String, V?>.filterNotNull(named: () -> String): KStream<String, V> =
    filter({ _, value -> value != null }, named(named())) as KStream<String, V>

fun <K, V> KStream<K, V>.filter(predicate: Predicate<in K, in V>, named: () -> String): KStream<K, V> =
    filter(predicate, named(named()))
