package personopplysninger.pdl.api

internal data class PdlResponse(val data: PdlData, val errors: List<PdlError>?) {
    val adressebeskyttelse get() = data.hentPerson?.adressebeskyttelse?.singleOrNull() ?: error("should exist?")
    val geografiskTilknytning get() = data.hentGeografiskTilknytning
}

internal data class PdlData(
    val hentGeografiskTilknytning: GeografiskTilknytning?,
    val hentPerson: Person?,
) {
    internal data class Person(val adressebeskyttelse: List<Adressebeskyttelse>)
    internal data class Adressebeskyttelse(val gradering: String)
    internal data class GeografiskTilknytning(val gtLand: String?, val gtKommune: String?, val gtBydel: String?)
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

