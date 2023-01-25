package personopplysninger.kafka

import no.nav.aap.kafka.SslConfig
import no.nav.aap.kafka.schemaregistry.SchemaRegistryConfig
import no.nav.aap.kafka.serde.avro.AvroSerde
import no.nav.aap.kafka.serde.avro.enum
import no.nav.aap.kafka.serde.avro.string
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer
import org.intellij.lang.annotations.Language
import personopplysninger.aktor.AktorDto
import personopplysninger.aktor.IdentifikatorDto
import personopplysninger.aktor.TypeDto
import java.util.*

class AktorAvroSerde : Serde<AktorDto> {
    private fun getRequiredEnv(env: String) = requireNotNull(
        System.getenv(env) ?: System.getProperty(env)
    ) { "Fant ikke påkrevd env: $env" }

    private val inner = AvroSerde.generic().apply {
        val schemaRegistry: Properties = SchemaRegistryConfig(
            url = getRequiredEnv("KAFKA_SCHEMA_REGISTRY"),
            user = getRequiredEnv("KAFKA_SCHEMA_REGISTRY_USER"),
            password = getRequiredEnv("KAFKA_SCHEMA_REGISTRY_PASSWORD"),
        ).properties()


        val ssl: Properties = SslConfig(
            truststorePath = getRequiredEnv("KAFKA_TRUSTSTORE_PATH"),
            keystorePath = getRequiredEnv("KAFKA_KEYSTORE_PATH"),
            credstorePsw = getRequiredEnv("KAFKA_CREDSTORE_PASSWORD")
        ).properties()

        val avroProperties = schemaRegistry + ssl
        val avroConfig = avroProperties.map { it.key.toString() to it.value.toString() }

        configure(avroConfig.toMap(), false)
    }

    override fun serializer(): Serializer<AktorDto> = AktorAvroSerializer(inner.serializer())
    override fun deserializer(): Deserializer<AktorDto> = AktorAvroDeserializer(inner.deserializer())
}

class AktorAvroDeserializer(private val inner: Deserializer<GenericRecord>) : Deserializer<AktorDto> {

    override fun deserialize(topic: String, data: ByteArray?): AktorDto? = data?.let {
        val record: GenericRecord = inner.deserialize(topic, data)
        val identifikatorer = record.get("identifikatorer") as GenericData.Array<*>

        AktorDto(
            identifikatorer = identifikatorer.filterIsInstance<GenericRecord>().map {
                IdentifikatorDto(
                    idnummer = it.string("idnummer")!!,
                    type = it.enum<TypeDto>("type")!!,
                    gjeldende = it.bool("gjeldende")!!
                )
            }
        )
    }
}

class AktorAvroSerializer(private val inner: Serializer<GenericRecord>) : Serializer<AktorDto> {

    private fun schema(json: String) = Schema.Parser().parse(json)

    override fun serialize(topic: String, data: AktorDto?): ByteArray? = data?.let { aktor ->
        val identifikatorer = aktor.identifikatorer.map { identifikator ->
            GenericData.Record(schema(identifikatorerJsonSchema)).apply {
                put("idnummer", identifikator.idnummer)
                put("type", GenericData.EnumSymbol(schema(typeJsonSchema), identifikator.type))
                put("gjeldende", identifikator.gjeldende)
            }
        }

        val record = GenericData.Record(schema(aktorJsonSchema)).apply {
            put("identifikatorer", GenericData.Array(schema(identifikatorJsonSchema), identifikatorer))
        }

        inner.serialize(topic, record)
    }

    @Language("Json")
    private val aktorJsonSchema = """ 
        {
    "type": "record",
    "name": "Aktor",
    "namespace": "no.nav.person.pdl.aktor.v2",
    "fields": [
      {
        "name": "identifikatorer",
        "type": {
          "type": "array",
          "items": {
            "type": "record",
            "name": "Identifikator",
            "fields": [
              {
                "name": "idnummer",
                "type": {
                  "type": "string",
                  "avro.java.string": "String"
                }
              },
              {
                "name": "type",
                "type": {
                  "type": "enum",
                  "name": "Type",
                  "symbols": [
                    "FOLKEREGISTERIDENT",
                    "AKTORID",
                    "NPID"
                  ]
                }
              },
              {
                "name": "gjeldende",
                "type": "boolean"
              }
            ]
          }
        }
      }
    ]
  }
    """.trimIndent()

    @Language("Json")
    private val identifikatorJsonSchema = """
        {
          "type": "array",
          "namespace": "no.nav.person.pdl.aktor.v2",
          "items": {
            "type": "record",
            "name": "Identifikator",
            "fields": [
              {
                "name": "idnummer",
                "type": {
                  "type": "string",
                  "avro.java.string": "String"
                }
              },
              {
                "name": "type",
                "type": {
                  "type": "enum",
                  "name": "Type",
                  "symbols": [
                    "FOLKEREGISTERIDENT",
                    "AKTORID",
                    "NPID"
                  ]
                }
              },
              {
                "name": "gjeldende",
                "type": "boolean"
              }
            ]
          }
        }
    """.trimIndent()

    @Language("Json")
    private val identifikatorerJsonSchema = """
        {
    "type": "record",
    "name": "Identifikator",
    "namespace": "no.nav.person.pdl.aktor.v2",
    "fields": [
      {
        "name": "idnummer",
        "type": {
          "type": "string",
          "avro.java.string": "String"
        }
      },
      {
        "name": "type",
        "type": {
          "type": "enum",
          "name": "Type",
          "symbols": [
            "FOLKEREGISTERIDENT",
            "AKTORID",
            "NPID"
          ]
        }
      },
      {
        "name": "gjeldende",
        "type": "boolean"
      }
    ]
  }
    """.trimIndent()

    @Language("Json")
    private val typeJsonSchema = """
        {
    "type": "enum",
    "name": "Type",
    "namespace": "no.nav.person.pdl.aktor.v2",
    "symbols": [
      "FOLKEREGISTERIDENT",
      "AKTORID",
      "NPID"
    ]
  }
    """.trimIndent()
}

fun GenericRecord.bool(name: String): Boolean? = get(name) as Boolean?
