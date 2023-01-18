package personopplysninger.streams

import no.nav.aap.kafka.serde.avro.array
import no.nav.aap.kafka.serde.avro.enum
import no.nav.aap.kafka.serde.avro.string
import no.nav.aap.kafka.streams.extension.consume
import no.nav.aap.kafka.streams.extension.filterNotNull
import no.nav.aap.kafka.streams.extension.produce
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.protocol.types.Field.Bool
import org.apache.kafka.streams.StreamsBuilder
import org.slf4j.LoggerFactory
import personopplysninger.kafka.Topics

private val secureLog = LoggerFactory.getLogger("secureLog")
internal fun StreamsBuilder.indentHendelseStream(){
    consume(Topics.identHendelser)
        .filterNotNull("skip-indenthendelse-tombstone")
        .mapValues { value ->
            secureLog.info(value.toString())
            val identifikatorer = ((value as GenericRecord).get("identifikatorer") as GenericData.Array<*>).map {
                val identifikatorRecord = it as GenericRecord
                Identifikator(
                    idnummer = identifikatorRecord.string("idnummer")!!,
                    type = identifikatorRecord.enum<Type>("type")!!,
                    gjeldende = identifikatorRecord.get("gjeldende")!! as Boolean
                )
            }
            Aktor(identifikatorer = identifikatorer)
        }.produce(Topics.test, "lol")
}
fun GenericRecord.array(name: String) = get(name) as GenericData.Array<*>

data class Aktor(
    val identifikatorer: List<Identifikator>,
)

data class Identifikator(
    val idnummer: String,
    val type: Type,
    val gjeldende: Boolean
)

enum class Type {
    FOLKEREGISTERIDENT,
    AKTORID,
    NPID
}