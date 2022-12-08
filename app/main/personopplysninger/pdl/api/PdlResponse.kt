package personopplysninger.pdl.api

internal data class PdlResponse(
    val data: PdlData?,
    val errors: List<PdlError>?,
) {
    val adressebeskyttelse get() = data?.hentPerson?.adressebeskyttelse?.singleOrNull()
}

internal data class PdlData(
    val hentGeografiskTilknytning: GeografiskTilknytning?,
    val hentPerson: Person?,
) {
    internal data class Person(
        val adressebeskyttelse: List<Adressebeskyttelse>,
        val navn: List<PdlNavn>,
    )
    internal data class Adressebeskyttelse(val gradering: String)
    internal data class GeografiskTilknytning(val gtLand: String?, val gtKommune: String?, val gtBydel: String?, val gtType: String)
    internal data class PdlNavn(val fornavn: String, val etternavn: String, val mellomnavn: String?)
}

internal data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation>,
    val path: List<String>?,
    val extensions: PdlErrorExtension
) {
    internal data class PdlErrorLocation(val line: Int?, val column: Int?)
    internal data class PdlErrorExtension(val code: String?, val classification: String)
}
