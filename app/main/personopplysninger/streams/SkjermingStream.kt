package personopplysninger.streams

import no.nav.aap.kafka.streams.extension.produce
import org.apache.kafka.streams.kstream.KStream
import personopplysninger.domain.Personopplysninger
import personopplysninger.kafka.Topics
import java.time.LocalDate
import java.time.LocalDateTime

fun KStream<String, SkjermetDto>.skjermingStream() =
    mapValues { _, skjerming ->
        val personopplysninger = Personopplysninger.opprettForOppdatering()
        personopplysninger.settSkjerming(skjerming.fom(), skjerming.tom())
        personopplysninger.toDto()
    }.produce(Topics.personopplysningerIntern, "reinitialize-personopplysninger-for-skjerming", true)

data class SkjermetDto(
    val skjermetFra: LocalDateTime,

    /** null betyr at den er skjermet til ubestemt tid*/
    val skjermetTil: LocalDateTime?,
) {
    fun fom(): LocalDate = skjermetFra.toLocalDate()
    fun tom(): LocalDate? = skjermetTil?.toLocalDate()
}
