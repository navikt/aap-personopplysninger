package personopplysninger.pdl.leesah

import domain.Adressebeskyttelse
import domain.Personopplysninger
import kotlinx.coroutines.runBlocking
import no.nav.aap.kafka.streams.consume
import no.nav.aap.kafka.streams.filterNotNull
import no.nav.aap.kafka.streams.produce
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KTable
import personopplysninger.Topics
import personopplysninger.pdl.api.PdlGraphQLClient

internal class LeesahStream(
    private val pdl: PdlGraphQLClient,
    personopplysninger: KTable<String, Personopplysninger>,
    kStreams: StreamsBuilder,
) {
    init {
        kStreams
            .consume(Topics.leesah) { "consume-pdl-leesah" }
            .filterNotNull()
            .filter(::isAdressebeskyttelse)
            .selectKey(::personident)
            .join(personopplysninger, ::merge, Topics.leesah.joined(Topics.personopplysninger))
            .mapValues(::toAddressebeskyttelse)
            .produce(Topics.personopplysninger) { "person-with-adressebeskyttelse" }
    }

    private fun isAdressebeskyttelse(hendelseId: String, hendelse: GenericRecord): Boolean =
        hendelse.string("opplysningstype") == ADRESSEBESKYTTELSE

    private fun personident(hendelseId: String, record: GenericRecord): String =
        record.array("personidenter").map(Any::toString).single { it.length == 11 }

    private fun toAddressebeskyttelse(personident: String, wrapper: Wrapper): Personopplysninger =
        when (val gradering = wrapper.left.generic("adressebeskyttelse")?.string("gradering")) {
            null -> Adressebeskyttelse(runBlocking { pdl.hentAdressebeskyttelse(personident).adressebeskyttelse }.gradering)
            else -> Adressebeskyttelse(gradering)
        }.let {
            val personopplysninger = wrapper.right
            personopplysninger.copy(adressebeskyttelse = it)
        }

    private data class Wrapper(val left: GenericRecord, val right: Personopplysninger)

    private fun merge(leesah: GenericRecord, person: Personopplysninger) = Wrapper(leesah, person)
}

// https://github.com/navikt/pdl/blob/master/libs/contract-pdl-avro/src/main/java/no/nav/person/identhendelse/Opplysningstype.java
private const val ADRESSEBESKYTTELSE = "ADRESSEBESKYTTELSE_V1"

private fun GenericRecord.generic(name: String): GenericRecord? = get(name) as GenericRecord?
private fun GenericRecord.string(name: String) = get(name)?.toString()
private fun GenericRecord.array(name: String) = get(name) as GenericData.Array<*>

private inline fun <reified V : Enum<V>> GenericRecord.enum(name: String) =
    get(name)?.let { enumValueOf<V>(it.toString()) }
