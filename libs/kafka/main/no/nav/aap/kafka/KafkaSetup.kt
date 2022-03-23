package no.nav.aap.kafka

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.*
import org.apache.kafka.streams.errors.ProductionExceptionHandler
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("app")
private val secureLog = LoggerFactory.getLogger("secureLog")

interface Kafka : AutoCloseable {
    fun start(kafkaConfig: KafkaConfig, streamsBuilder: StreamsBuilder.() -> Unit)
    fun <V : Any> createProducer(topic: Topic<String, V>): Producer<String, V>
    fun <V : Any> createConsumer(topic: Topic<String, V>): Consumer<String, V>
    fun <V> getStore(name: String): ReadOnlyKeyValueStore<String, V>
    fun healthy(): Boolean
    fun started(): Boolean
}

class KafkaSetup : Kafka {
    private lateinit var config: KafkaConfig
    private lateinit var streams: KafkaStreams
    private var started: Boolean = false

    override fun start(kafkaConfig: KafkaConfig, streamsBuilder: StreamsBuilder.() -> Unit) {
        val topology = StreamsBuilder().apply(streamsBuilder).build()
        streams = KafkaStreams(topology, kafkaConfig.consumer + kafkaConfig.producer)
        streams.setUncaughtExceptionHandler { err: Throwable ->
            secureLog.error("Uventet feil, logger og leser neste record, ${err.message}", err)
            StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD
        }
        streams.setStateListener { newState, oldState ->
            log.info("Kafka streams state changed: $oldState -> $newState")
            if (newState == KafkaStreams.State.RUNNING) started = true
        }
        config = kafkaConfig
        streams.start()
    }

    override fun <V> getStore(name: String): ReadOnlyKeyValueStore<String, V> =
        streams.store(StoreQueryParameters.fromNameAndType(name, QueryableStoreTypes.keyValueStore()))

    override fun started() = started
    override fun close() = streams.close()
    override fun healthy(): Boolean = streams.state() in listOf(
        KafkaStreams.State.CREATED,
        KafkaStreams.State.RUNNING,
        KafkaStreams.State.REBALANCING
    )

    override fun <V : Any> createConsumer(topic: Topic<String, V>): Consumer<String, V> =
        KafkaConsumer(
            config.consumer + mapOf(CommonClientConfigs.CLIENT_ID_CONFIG to "client-${topic.name}"),
            topic.keySerde.deserializer(),
            topic.valueSerde.deserializer()
        )

    override fun <V : Any> createProducer(topic: Topic<String, V>): Producer<String, V> =
        KafkaProducer(
            config.producer + mapOf(CommonClientConfigs.CLIENT_ID_CONFIG to "client-${topic.name}"),
            topic.keySerde.serializer(),
            topic.valueSerde.serializer()
        )
}

fun named(named: String): Named = Named.`as`(named)

fun <K, V> materialized(
    storeName: String,
    topic: Topic<K, V>,
): Materialized<K, V, KeyValueStore<Bytes, ByteArray>> =
    Materialized.`as`<K?, V, KeyValueStore<Bytes, ByteArray>?>(storeName)
        .withKeySerde(topic.keySerde)
        .withValueSerde(topic.valueSerde)

fun <V> ReadOnlyKeyValueStore<String, V>.allValues(): List<V> =
    all().use { it.asSequence().map(KeyValue<String, V>::value).toList() }

class LogContinueErrorHandler : ProductionExceptionHandler {
    override fun configure(configs: MutableMap<String, *>?) {}
    override fun handle(
        record: ProducerRecord<ByteArray, ByteArray>?,
        exception: Exception?
    ): ProductionExceptionHandler.ProductionExceptionHandlerResponse {
        secureLog.error("Feil i streams, logger og leser neste record", exception)
        return ProductionExceptionHandler.ProductionExceptionHandlerResponse.CONTINUE
    }
}

fun <K, V> StreamsBuilder.consume(topic: Topic<K, V>, named: () -> String): KStream<K, V> =
    stream(topic.name, topic.consumed(named()))
        .peek { key, value -> secureLog.info("consumed [${topic.name}] K:$key V:$value") }

fun <K, V> KStream<K, V>.to(topic: Topic<K, V>, producedWith: Produced<K, V>) = this
    .peek { key, value -> secureLog.info("produced [${topic.name}] K:$key V:$value") }
    .to(topic.name, producedWith)

fun <K, V> KStream<K, V>.toTable(table: Table<K, V>) {
    peek { key, value -> secureLog.info("produced [${table.stateStoreName}] K:$key V:$value") }
        .toTable(named("${table.name}-as-ktable"), materialized(table.stateStoreName, table.source))
}
