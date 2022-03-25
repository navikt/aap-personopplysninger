package nom.skjerming

import java.time.LocalDateTime

data class SkjermetDto(
    val skjermetFra: LocalDateTime,

    /** null betyr at den er skjermet til ubestemt tid*/
    val skjermetTil: LocalDateTime?,
)
