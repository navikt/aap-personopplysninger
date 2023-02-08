package personopplysninger.streams

import no.nav.aap.kafka.streams.v2.KTable
import no.nav.aap.kafka.streams.v2.KeyValue
import no.nav.aap.kafka.streams.v2.Topology
import no.nav.aap.kafka.streams.v2.processor.ProcessorMetadata
import no.nav.aap.kafka.streams.v2.processor.state.StateProcessor
import no.nav.aap.kafka.streams.v2.stream.MappedKStream
import org.apache.kafka.streams.state.TimestampedKeyValueStore
import org.slf4j.LoggerFactory
import personopplysninger.aktor.AktorDto
import personopplysninger.aktor.IdentifikatorDto
import personopplysninger.aktor.TypeDto
import personopplysninger.kafka.Topics
import java.util.*

private val secureLog = LoggerFactory.getLogger("secureLog")
private val log = LoggerFactory.getLogger("app")

internal fun Topology.aktørStream(søkereKTable: KTable<ByteArray>) {
    consume(Topics.aktørV2, true)
        .flatMapKeyAndValuePreserveType { _, value ->
            value.identifikatorer
                // trenger ikke bytte hvis vi allerede har gjeldende
                .filter { it.type == TypeDto.FOLKEREGISTERIDENT && !it.gjeldende }
                .map { it.idnummer }
                .associateWith { value }
                .map { (key, value) -> KeyValue(key, value) }
        }
        .repartition(12)
        .processor(PersonidenterLookupTransformer(søkereKTable))
        .branch({ (_, søkere) -> søkere.size == 1 }, ::oppdaterSøkersPersonident)
        .branch({ (_, søkere) -> søkere.size > 1 }, ::varsleOmFlerSøknaderForSammenslåttePersonidentifikatorer)
}

internal fun oppdaterSøkersPersonident(stream: MappedKStream<AktørAndSøkere>) {
    stream
        .log { _, _ -> secureLog.info("Forsøker å oppdatere søkers personident") }
        .mapKeyAndValue { _, (aktør, søkere) ->
            val endretPersonident = aktør.identifikatorer
                .filter { it.type == TypeDto.FOLKEREGISTERIDENT }
                .single(IdentifikatorDto::gjeldende)
                .idnummer

            val forrigePersonident = søkere.single().key
            KeyValue(forrigePersonident, endretPersonident)
        }
        .produce(Topics.endredePersonidenter, true)
}

internal fun varsleOmFlerSøknaderForSammenslåttePersonidentifikatorer(stream: MappedKStream<AktørAndSøkere>) {
    stream.log { _, (aktør, søkere) ->
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

internal class PersonidenterLookupTransformer(ktable: KTable<ByteArray>) :
    StateProcessor<ByteArray, AktorDto, AktørAndSøkere>(
        named = "lookup-personidenter-${Topics.aktørV2.name}",
        table = ktable,
    ) {
    override fun process(
        metadata: ProcessorMetadata,
        store: TimestampedKeyValueStore<String, ByteArray>,
        keyValue: KeyValue<String, AktorDto>
    ): AktørAndSøkere {
        val folkeregIdenter = keyValue.value.identifikatorer
            .filter { it.type == TypeDto.FOLKEREGISTERIDENT }
            .map { it.idnummer }

        val søkere = folkeregIdenter
            .mapNotNull { ident -> store[ident]?.let { Søker(ident) } }
            .onEach { secureLog.info("Fant: ${it.key}") }

        return AktørAndSøkere(keyValue.value, søkere)
    }
}

internal data class AktørAndSøkere(val aktør: AktorDto, val søkere: List<Søker>)
internal data class Søker(val key: String)
