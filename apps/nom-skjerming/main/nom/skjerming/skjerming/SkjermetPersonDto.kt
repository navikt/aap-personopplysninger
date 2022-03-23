package nom.skjerming.skjerming

import java.time.LocalDateTime

data class SkjermetPersonDto(
    val skjermetFra: LocalDateTime,

    /** null betyr at den er skjermet til ubestemt tid*/
    val skjermetTil: LocalDateTime?,
)
