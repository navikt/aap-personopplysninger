package personopplysninger.norg

import model.Personopplysninger.PersonopplysningerDto

internal object Arbeidsfordeling {
    fun createRequest(personDto: PersonopplysningerDto) = ArbeidsfordelingRequest(
        geografiskOmraade = personDto.geografiskTilknytning ?: error("GT skal ikke være null her"),
        skjermet = personDto.skjerming?.erSkjermet ?: error("skjermet skal ikke være null her"),
        diskresjonskode = when (personDto.adressebeskyttelse) {
            "STRENGT_FORTROLIG", "STRENGT_FORTROLIG_UTLAND" -> "SPSF"
            "FORTROLIG" -> "SPFO"
            "UGRADERT" -> "ANY"
            else -> error("ukjent adressebeskyttelse ${personDto.adressebeskyttelse}")
        }
    )
}

data class ArbeidsfordelingResponse(val enhetNr: String)

data class ArbeidsfordelingRequest(
    val geografiskOmraade: String,
    val skjermet: Boolean,
    val diskresjonskode: String,
    private val tema: String = "AAP",
    private val behandlingstema: String = Behandlingstema.`§11-5`,
)

private object Behandlingstema {
    const val `§11-5` = "abNNNN" // TODO: bestille hos NORG
}
