package personopplysninger.rest

import personopplysninger.domain.PersonopplysningerDto

data class ArbeidsfordelingDtoResponse(
    val enhetNr: String,
)

data class ArbeidsfordelingDtoRequest(
    val geografiskOmraade: String,
    val skjermet: Boolean,
    val diskresjonskode: String,
    val tema: String = "AAP",
    val behandlingstema: String = Behandlingstema.ARBEIDSAVKLARINGSPENGER,
) {
    private object Behandlingstema {
        const val ARBEIDSAVKLARINGSPENGER = "ab0014"
    }

    companion object {
        fun create(personDto: PersonopplysningerDto) = ArbeidsfordelingDtoRequest(
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
}
