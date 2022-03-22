package pdl.api

data class PdlResponse(
    val data: PdlData,
    val errors: List<PdlError>?,
)

data class PdlData(
    val hentGeografiskTilknytning: GeografiskTilknytning?,
    val hentPerson: Person?,
)

data class Person(val adressebeskyttelse: List<Adressebeskyttelse>, )
data class Adressebeskyttelse(val gradering: String)

data class GeografiskTilknytning(
    val gtLand: String?,
    val gtKommune: String?,
    val gtBydel: String?,
)

data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation>,
    val path: List<String>?,
    val extensions: PdlErrorExtension
)

data class PdlErrorLocation(
    val line: Int?,
    val column: Int?
)

data class PdlErrorExtension(
    val code: String?,
    val classification: String
)