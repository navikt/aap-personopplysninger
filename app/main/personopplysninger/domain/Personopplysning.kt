package personopplysninger.domain

import java.time.LocalDate

class Personopplysninger private constructor(
    private var norgEnhetId: String? = null,
    private var adresseeskyttelse: String? = null,
    private var geografiskTilknytning: String? = null,
    private var skjerming: Skjerming? = null,
) {
    fun settEnhet(id: String) {
        if (norgEnhetId == null) norgEnhetId = id
    }

    fun settSkjerming(fom: LocalDate?, tom: LocalDate?) {
        if (skjerming == null) skjerming = Skjerming(fom, tom)
    }

    fun settAdressebeskyttelse(gradering: String) {
        if (adresseeskyttelse == null) adresseeskyttelse = gradering
    }

    fun settGeografiskTilknytning(gt: String) {
        if (geografiskTilknytning == null) geografiskTilknytning = gt
    }

    private class Skjerming(val fom: LocalDate?, val tom: LocalDate?) {
        fun erSkjermet(): Boolean = fom != null && (tom == null || tom >= LocalDate.now())
    }

    companion object {
        fun opprettForOppdatering() = Personopplysninger()

        fun restore(dto: PersonopplysningerDto) = Personopplysninger(
            norgEnhetId = dto.norgEnhetId,
            adresseeskyttelse = dto.adressebeskyttelse,
            geografiskTilknytning = dto.geografiskTilknytning,
            skjerming = dto.skjerming?.let { Skjerming(it.fom, it.tom) }
        )
    }

    fun toDto() = PersonopplysningerDto(
        norgEnhetId = norgEnhetId,
        adressebeskyttelse = adresseeskyttelse,
        geografiskTilknytning = geografiskTilknytning,
        skjerming = skjerming?.let { SkjermingDto(it.erSkjermet(), it.fom, it.tom) }
    )
}

data class PersonopplysningerDto(
    val norgEnhetId: String? = null,
    val adressebeskyttelse: String? = null,
    val geografiskTilknytning: String? = null,
    val skjerming: SkjermingDto? = null,
) {
    fun kanSetteSkjerming(): Boolean = skjerming == null
    fun kanSetteGraderingEllerGT(): Boolean = adressebeskyttelse == null || geografiskTilknytning == null
    fun kanSetteEnhet(): Boolean = !kanSetteSkjerming() && !kanSetteGraderingEllerGT() && norgEnhetId == null
}

data class SkjermingDto(
    val erSkjermet: Boolean,
    val fom: LocalDate?,
    val tom: LocalDate?
)
