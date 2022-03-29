package model

import java.time.LocalDate

class Personopplysninger {
    private var norgEnhetId: String? = null
    private var adresseeskyttelse: String? = null
    private var gt: String? = null
    private var skjerming: Skjerming? = null

    private inner class Skjerming(val fom: LocalDate?, val tom: LocalDate?) {
        fun erSkjermet(): Boolean = tom == null || tom >= LocalDate.now()
    }

    fun settEnhet(enhetId: String) = let { if (norgEnhetId == null) norgEnhetId = enhetId }
    fun settSkjerming(fom: LocalDate?, tom: LocalDate?) = let { if (skjerming == null) skjerming = Skjerming(fom, tom) }
    fun settAdressebeskyttelse(gradering: String) = let { if (adresseeskyttelse == null) adresseeskyttelse = gradering }
    fun settTilhørendeBydel(bydel: String) = let { if (gt == null) gt = bydel }
    fun settTilhørendeKommune(kommune: String) = let { if (gt == null) gt = kommune }
    fun settTilhørendeLand(land: String) = let { if (gt == null) gt = land }

    fun kanSetteEnhet() = norgEnhetId == null && listOf(adresseeskyttelse, gt, skjerming).all { it != null }
    fun kanSetteAdressebeskyttelse() = adresseeskyttelse == null
    fun kanSetteGeografiskTilknytning() = gt == null
    fun kanSetteSkjerming() = skjerming == null

    fun toDto() = PersonopplysningerDto(
        norgEnhetId = norgEnhetId!!,
        adressebeskyttelse = adresseeskyttelse!!,
        geografiskTilknytning = gt!!,
        skjerming = SkjermingDto(skjerming!!.erSkjermet(), skjerming!!.fom, skjerming!!.tom)
    )

    data class PersonopplysningerDto(
        val norgEnhetId: String,
        val adressebeskyttelse: String,
        val geografiskTilknytning: String,
        val skjerming: SkjermingDto,
    )

    data class SkjermingDto(
        val erSkjermet: Boolean,
        val fom: LocalDate?,
        val tom: LocalDate?
    )
}
