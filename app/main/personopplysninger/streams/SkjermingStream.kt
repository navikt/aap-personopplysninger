package personopplysninger.streams

import no.nav.aap.kafka.streams.extension.produce
import org.apache.kafka.streams.kstream.KStream
import personopplysninger.domain.Personopplysninger
import personopplysninger.kafka.Topics
import java.time.LocalDateTime

fun KStream<String, SkjermetDto>.skjermingStream() =
    mapValues { _, skjerming ->
        val personopplysninger = Personopplysninger.opprettForOppdatering()
        personopplysninger.settSkjerming(skjerming.skjermetFra.toLocalDate(), skjerming.skjermetTil?.toLocalDate())
        personopplysninger.toDto()
    }.produce(Topics.personopplysninger, "reinitialize-personopplysninger-for-skjerming")

data class SkjermetDto(
    val skjermetFra: LocalDateTime,

    /** null betyr at den er skjermet til ubestemt tid*/
    val skjermetTil: LocalDateTime?,
)
