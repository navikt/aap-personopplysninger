package personopplysninger.pdl.api

internal data class PdlRequest(val query: String, val variables: Variables) {
    data class Variables(val ident: String)

    companion object {
        fun hentAlt(personident: String) = PdlRequest(alt, Variables(personident))
        fun hentGeografiskTilknytning(personident: String) = PdlRequest(geografiskTilknytning, Variables(personident))
        fun hentAdressebeskyttelse(personident: String) = PdlRequest(adressebeskyttelse, Variables(personident))
    }
}

private const val ident = "\$ident" // workaround to escape $ in multiline string

private const val alt = """
    query($ident: ID!) {
        hentGeografiskTilknytning(ident: $ident) {
            gtKommune
            gtBydel
            gtLand
        }
        hentPerson(ident: $ident) {
            adressebeskyttelse {
                gradering
            }
        }
    }
"""

private const val geografiskTilknytning = """
    query($ident: ID!) {
        hentGeografiskTilknytning(ident: $ident) {
            gtKommune
            gtBydel
            gtLand
        }
    }
"""

private const val adressebeskyttelse = """
    query($ident: ID!) {
        hentPerson(ident: $ident) {
            adressebeskyttelse {
                gradering
            }
        }
    }
"""
