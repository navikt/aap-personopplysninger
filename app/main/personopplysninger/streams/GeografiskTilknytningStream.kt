package personopplysninger.streams

import no.nav.aap.kafka.streams.extension.produce
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import personopplysninger.domain.Personopplysninger
import personopplysninger.kafka.Topics

internal fun StreamsBuilder.geografiskTilknytningStream() =
    stream(
        Topics.geografiskTilknytning.name,
        Consumed
            .with(Topics.geografiskTilknytning.keySerde, Topics.geografiskTilknytning.valueSerde)
            .withOffsetResetPolicy(Topology.AutoOffsetReset.LATEST)
    ).flatMap { _, gt ->
        gt.identer.map { ident ->
            val personopplysninger = Personopplysninger.opprettForOppdatering()
            personopplysninger.settGeografiskTilknytning(gt.geografiskTilknytning())
            KeyValue.pair(ident, personopplysninger.toDto())
        }
    }.produce(Topics.personopplysningerIntern, "reinitialize-personopplysninger-for-gt")

data class GtMedIdenter(
    val identer: List<String>,
    private val geografiskTilknytning: GeografiskTilknytning,
) {
    internal fun geografiskTilknytning() = geografiskTilknytning.get()
}

data class GeografiskTilknytning(
    private val gtType: String,
    private val gtKommune: String?,
    private val gtBydel: String?,
    private val gtLand: String?,
    private val regel: String,
) {
    internal fun get(): String = gtBydel ?: gtKommune ?: gtLand ?: gtType
}
