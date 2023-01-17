package personopplysninger.domain

import java.time.LocalDate

class Personopplysninger private constructor(
    private var norgEnhetId: String? = null,
    private var adresseeskyttelse: String? = null,
    private var geografiskTilknytning: String? = null,
    private var skjerming: Skjerming? = null,
    private var navn: Navn? = null,
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

    fun settNavn(fornavn: String, mellomnavn: String?, etternavn: String) {
        if (navn == null) navn = Navn(fornavn, mellomnavn, etternavn)
    }

    private class Skjerming(val fom: LocalDate?, val tom: LocalDate?) {
        fun erSkjermet(): Boolean = fom != null && (tom == null || tom >= LocalDate.now())
    }

    private class Navn(val fornavn: String, val mellomnavn: String?, val etternavn: String)

    companion object {
        fun opprettForOppdatering() = Personopplysninger()

        fun restore(dto: PersonopplysningerDto) = Personopplysninger(
            norgEnhetId = dto.norgEnhetId,
            adresseeskyttelse = dto.adressebeskyttelse,
            geografiskTilknytning = dto.geografiskTilknytning,
            skjerming = dto.skjerming?.let { Skjerming(it.fom, it.tom) },
            navn = dto.navn?.let { Navn(it.fornavn, it.mellomnavn, it.etternavn) }
        )
    }

    fun toDto() = PersonopplysningerDto(
        norgEnhetId = norgEnhetId,
        adressebeskyttelse = adresseeskyttelse,
        geografiskTilknytning = geografiskTilknytning,
        skjerming = skjerming?.let { SkjermingDto(it.erSkjermet(), it.fom, it.tom) },
        navn = navn?.let { NavnDto(it.fornavn, it.mellomnavn, it.etternavn) }
    )
}

data class PersonopplysningerDto(
    val norgEnhetId: String? = null,
    val adressebeskyttelse: String? = null,
    val geografiskTilknytning: String? = null,
    val skjerming: SkjermingDto? = null,
    val navn: NavnDto? = null,
) {
    fun kanSetteSkjerming(): Boolean = skjerming == null
    fun kanSettePdlopplysninger(): Boolean = adressebeskyttelse == null || geografiskTilknytning == null || navn == null
    fun kanSetteEnhet(): Boolean = !kanSetteSkjerming() && !kanSettePdlopplysninger() && norgEnhetId == null
}

data class SkjermingDto(
    val erSkjermet: Boolean,
    val fom: LocalDate?,
    val tom: LocalDate?
)

data class NavnDto(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
)