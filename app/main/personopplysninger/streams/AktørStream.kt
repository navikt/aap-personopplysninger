package personopplysninger.streams

import no.nav.aap.kafka.streams.extension.consume
import no.nav.aap.kafka.streams.extension.filterNotNull
import no.nav.aap.kafka.streams.extension.produce
import no.nav.aap.kafka.streams.named
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Branched
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Repartitioned
import org.apache.kafka.streams.processor.api.FixedKeyProcessor
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext
import org.apache.kafka.streams.processor.api.FixedKeyRecord
import org.apache.kafka.streams.state.TimestampedKeyValueStore
import org.apache.kafka.streams.state.ValueAndTimestamp
import org.slf4j.LoggerFactory
import personopplysninger.aktor.AktorDto
import personopplysninger.aktor.IdentifikatorDto
import personopplysninger.aktor.TypeDto
import personopplysninger.kafka.Tables
import personopplysninger.kafka.Topics
import java.util.*

private val secureLog = LoggerFactory.getLogger("secureLog")
private val log = LoggerFactory.getLogger("app")

internal fun StreamsBuilder.aktørStream() {
    consume(Topics.aktørV2, true)
        .filterNotNull("skip-indenthendelse-tombstone")
        .flatMap { _, value ->
            value.identifikatorer
                // trenger ikke bytte hvis vi allerede har gjeldende
                .filter { it.type == TypeDto.FOLKEREGISTERIDENT && !it.gjeldende}
                .map { it.idnummer }
                .associateWith { value }
                .map { (key, value) -> KeyValue(key, value) }
        }
        .repartition(Repartitioned.with(Topics.aktørV2.keySerde, Topics.aktørV2.valueSerde).withNumberOfPartitions(12))
        .lookupPersonidenter()
        .split()
        .branch({ _, (_, søkere) -> søkere.size == 1 }, oppdaterSøkersPersonident())
        .branch({ _, (_, søkere) -> søkere.size > 1 }, varsleOmFlerSøknaderForSammenslåttePersonidentifikatorer())
        .noDefaultBranch()
}

internal fun oppdaterSøkersPersonident(): Branched<String, AktørAndSøkere> =
    Branched.withConsumer { kstream ->
        kstream
            .peek { _, _ -> secureLog.info("Forsøker å oppdatere søkers personident") }
            .map { _, (aktør, søkere) ->
                val endretPersonident = aktør.identifikatorer
                    .filter { it.type == TypeDto.FOLKEREGISTERIDENT }
                    .single(IdentifikatorDto::gjeldende)
                    .idnummer

                val forrigePersonident = søkere.single().key
                KeyValue(forrigePersonident, endretPersonident)
            }.produce(Topics.endredePersonidenter, "endrer-personident", true)
    }

internal fun varsleOmFlerSøknaderForSammenslåttePersonidentifikatorer(): Branched<String, AktørAndSøkere> =
    Branched.withConsumer { kstream ->
        kstream.peek { _, (aktør, søkere) ->
            secureLog.info("Forsøker å varsle om fler søknader på samme person")
            val aapSøkere = søkere.map { it.key }
            val folkeregisteridenter = aktør.identifikatorer.filter { it.type == TypeDto.FOLKEREGISTERIDENT }
            val sammenslåtteIdenter = folkeregisteridenter.map { it.idnummer }
            val gjeldendeIdent = folkeregisteridenter.single { it.gjeldende }.idnummer

            val referanse = UUID.randomUUID().toString()
            log.error("Oppdaget merge/splitt av personidenter. Log-referanse: $referanse")
            secureLog.error(
                """
                Mottok melding fra ${Topics.aktørV2} om sammenslåing av personidenter
                Registrerte søkere i AAP: $aapSøkere
                Sammenslåtte personidenter: $sammenslåtteIdenter
                Gjeldende ident: $gjeldendeIdent
                Dette må håndteres manuelt av teamet og utviklerne.
                Log-referanse: $referanse
                """.trimIndent()
            )
        }
    }

internal class PersonidenterLookupTransformer : FixedKeyProcessor<String, AktorDto, AktørAndSøkere> {
    private lateinit var context: FixedKeyProcessorContext<String, AktørAndSøkere>
    private lateinit var store: TimestampedKeyValueStore<String, ValueAndTimestamp<ByteArray>>

    override fun init(ctxt: FixedKeyProcessorContext<String, AktørAndSøkere>) {
        context = ctxt
        store = context.getStateStore(Tables.søkere.stateStoreName)
    }

    override fun process(record: FixedKeyRecord<String, AktorDto>) {
        secureLog.info("Sjekker søkere KTable etter ident")
        val aktør = record.value()
        val folkeregIdenter = aktør.identifikatorer
            .filter { it.type == TypeDto.FOLKEREGISTERIDENT }
            .map { it.idnummer }

        val søkere = folkeregIdenter.mapNotNull { ident ->
            val temp = "19469120611"
            secureLog.info("Leter etter $ident, ${ident.hashCode()} == ${temp.hashCode()} (${store[ident]})")
            store[ident]?.let {
                Søker(ident)
            }

        }
        søkere.forEach {
            secureLog.info("Fant: ${it.key}")
        }

        context.forward(record.withValue(AktørAndSøkere(aktør, søkere)))
    }

    override fun close() {}
}

internal data class AktørAndSøkere(val aktør: AktorDto, val søkere: List<Søker>)
internal data class Søker(val key: String)

internal fun KStream<String, AktorDto>.lookupPersonidenter(): KStream<String, AktørAndSøkere> =
    processValues(
        { PersonidenterLookupTransformer() },
        named("lookup-personidenter-${Topics.aktørV2.name}"),
        Tables.søkere.stateStoreName
    )
