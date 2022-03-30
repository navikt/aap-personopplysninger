package model

import java.time.LocalDate

class Personopplysninger private constructor(
    private var norgEnhetId: String? = null,
    private var adresseeskyttelse: String? = null,
    private var gt: String? = null,
    private var skjerming: Skjerming? = null,
) {
    private class Skjerming(val fom: LocalDate?, val tom: LocalDate?) {
        fun erSkjermet(): Boolean = fom != null && (tom == null || tom >= LocalDate.now())
    }

    fun settEnhet(enhetId: String) = let { if (norgEnhetId == null) norgEnhetId = enhetId }
    fun settSkjerming(fom: LocalDate?, tom: LocalDate?) = let { if (skjerming == null) skjerming = Skjerming(fom, tom) }
    fun settTilhørendeBydel(bydel: String) = let { if (gt == null) gt = bydel }
    fun settTilhørendeKommune(kommune: String) = let { if (gt == null) gt = kommune }
    fun settTilhørendeLand(land: String) = let { if (gt == null) gt = land }
    fun settAdressebeskyttelse(gradering: String?) = let {
        if (adresseeskyttelse == null) adresseeskyttelse = gradering ?: "UGRADERT"
    }

    fun kanSetteEnhet() = norgEnhetId == null && listOf(adresseeskyttelse, gt, skjerming).all { it != null }
    fun kanSetteAdressebeskyttelse() = adresseeskyttelse == null
    fun kanSetteGeografiskTilknytning() = gt == null
    fun kanSetteSkjerming() = skjerming == null

    fun toDto() = PersonopplysningerDto(
        norgEnhetId = norgEnhetId,
        adressebeskyttelse = adresseeskyttelse,
        geografiskTilknytning = gt,
        skjerming = skjerming?.let { SkjermingDto(it.erSkjermet(), it.fom, it.tom) }
    )

    data class PersonopplysningerDto(
        val norgEnhetId: String? = null,
        val adressebeskyttelse: String? = null,
        val geografiskTilknytning: String? = null,
        val skjerming: SkjermingDto? = null,
    ) {
        fun restore() = Personopplysninger(
            norgEnhetId = norgEnhetId,
            adresseeskyttelse = adressebeskyttelse,
            gt = geografiskTilknytning,
            skjerming = skjerming?.let { Skjerming(it.fom, it.tom) }
        )
    }

    data class SkjermingDto(
        val erSkjermet: Boolean,
        val fom: LocalDate?,
        val tom: LocalDate?
    )
}
