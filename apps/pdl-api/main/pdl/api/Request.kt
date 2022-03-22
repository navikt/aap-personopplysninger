package pdl.api

data class GraphqlQuery(val query: String, val variables: Variables)
data class Variables(val ident: String)

fun query(personident: String) = GraphqlQuery(query, Variables(personident))

private const val ident = "\$ident" // workaround to escape $ in multiline string

private const val query = """
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
