package personopplysninger.pdl.streams

import personopplysninger.Personopplysninger
import no.nav.aap.kafka.serde.avro.array
import no.nav.aap.kafka.serde.avro.generic
import no.nav.aap.kafka.serde.avro.string
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.streams.StreamsBuilder

fun StreamsBuilder.leesahStream() = this
//    .consume(Topics.leesah)
//    .filterNotNull { "skip-leesah-tombstone" }
//    .filter(::isAdressebeskyttelse) { "filter-is-adressebeskyttelse" }
//    .selectKey("name") { _, value -> value.personidenter.single { it.length == 11 } }
//    .mapValues(::toAddressebeskyttelse)
//    .mapValues(Personopplysninger::toDto)
//    .produce(Topics.personopplysninger) { "person-with-adressebeskyttelse" }

// https://github.com/navikt/pdl/blob/master/libs/contract-pdl-avro/src/main/java/no/nav/person/identhendelse/Opplysningstype.java
private fun isAdressebeskyttelse(personident: String, hendelse: GenericRecord): Boolean {
    return hendelse.opplysningstype == Opplysningstype.ADRESSEBESKYTTELSE
}

private fun toAddressebeskyttelse(person: GenericRecord) = Personopplysninger.opprettForOppdatering().apply {
    settAdressebeskyttelse(person.gradering ?: "UGRADERT")
}

private object Opplysningstype {
    const val ADRESSEBESKYTTELSE = "ADRESSEBESKYTTELSE_V1"
}

private val GenericRecord.opplysningstype get() = string("opplysningstype")
private val GenericRecord.personidenter get() = array("personidenter").map(Any::toString)
private val GenericRecord.gradering get() = generic("adressebeskyttelse")?.string("gradering")
