package no.nav.aap.kafka

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer
import kotlin.reflect.KClass

fun <T : SpecificRecord> avroSerde(config: KafkaConfig): SpecificAvroSerde<T> = SpecificAvroSerde<T>().apply {
    val avroProperties = config.schemaRegistry + config.ssl
    val avroConfig = avroProperties.map { it.key.toString() to it.value.toString() }
    configure(avroConfig.toMap(), false)
}

class JsonSerde<V : Any>(private val kclass: KClass<V>) : Serde<V> {
    override fun serializer(): Serializer<V> = JsonSerializer()
    override fun deserializer(): Deserializer<V> = JsonDeserializer(kclass)

    companion object {
        inline fun <reified V : Any> create() = JsonSerde(V::class)
    }
}

class JsonDeserializer<T : Any>(private val kclass: KClass<T>) : Deserializer<T> {
    private val jackson: ObjectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    override fun deserialize(topic: String, data: ByteArray?): T? = jackson.readValue(data, kclass.java)
}

class JsonSerializer<T : Any> : Serializer<T> {
    private val jackson: ObjectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    override fun serialize(topic: String, data: T?): ByteArray? = data?.let { jackson.writeValueAsBytes(it) }
}
