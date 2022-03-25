package domain

data class Personopplysninger(
    val adressebeskyttelse: Adressebeskyttelse? = null,
    val skjerming: Skjerming? = null,
)

data class Adressebeskyttelse(
    val gradering: String
)

data class Skjerming(
    val erSkjermet: Boolean
)
