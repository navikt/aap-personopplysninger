package domain

data class Personopplysninger(
    val adressebeskyttelse: Adressebeskyttelse? = null, // ferdig når den ikke er null
    val skjerming: Skjerming? = null, // sett til default false dersom det ikke finnes noe skjerming
    val geografiskTilknytning: GeografiskTilknytning? = null,
)

data class GeografiskTilknytning(
    val gtLand: String?,
    val gtKommune: String?,
    val gtBydel: String?,
)

data class Adressebeskyttelse(
    val gradering: String
)

data class Skjerming(
    val erSkjermet: Boolean
)
