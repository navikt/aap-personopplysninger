package personopplysninger.aktor


data class AktorDto(
    val identifikatorer: List<IdentifikatorDto>,
)

data class IdentifikatorDto(
    val idnummer: String,
    val type: TypeDto,
    val gjeldende: Boolean
)

enum class TypeDto {
    FOLKEREGISTERIDENT,
    AKTORID,
    NPID
}