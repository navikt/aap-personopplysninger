package personopplysninger.mocks

import io.ktor.http.ContentType.Application.Json
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.intellij.lang.annotations.Language

const val KOMMUNE_PERSON = "11111111111"
const val BYDEL_PERSON = "22222222222"
const val SVENSK_PERSON = "33333333333"
const val UGRADERT_PERSON = "44444444444"
const val FORTROLIG_PERSON = "55555555555"
const val STRENGT_FORTROLIG_PERSON = "66666666666"
const val STRENGT_FORTROLIG_UTLAND_PERSON = "77777777777"
const val PERSON_UTEN_GRADERING = "88888888888"

internal fun Application.pdlApiMock() = routing {
    post("/graphql") {
        require(call.request.headers["Authorization"] == "Bearer very.secure.token") { "missing token" }
        require(call.request.headers["TEMA"] == "AAP") { "missing tema" }
        val response = call.receiveText()
        when {
            response.contains(KOMMUNE_PERSON) -> call.respondText(gtKommune, Json)
            response.contains(BYDEL_PERSON) -> call.respondText(gtBydel, Json)
            response.contains(SVENSK_PERSON) -> call.respondText(gtLand, Json)
            response.contains(UGRADERT_PERSON) -> call.respondText(ugradert, Json)
            response.contains(FORTROLIG_PERSON) -> call.respondText(fortrolig, Json)
            response.contains(STRENGT_FORTROLIG_PERSON) -> call.respondText(strengtFortrolig, Json)
            response.contains(STRENGT_FORTROLIG_UTLAND_PERSON) -> call.respondText(strengtFortroligUtland, Json)
            response.contains(PERSON_UTEN_GRADERING) -> call.respondText(ukjent, Json)
            else -> call.respondText(gtKommune, Json)
        }
    }
}

@Language("JSON")
private const val gtKommune = """
{
  "data": {
    "hentGeografiskTilknytning": {
      "gtKommune": "0301",
      "gtBydel": null,
      "gtLand": null,
      "gtType": "KOMMUNE"
    },
    "hentPerson": {
      "adressebeskyttelse": []
    }
  },
  "extensions": {}
}
"""

@Language("JSON")
private const val gtBydel = """
{
  "data": {
    "hentGeografiskTilknytning": {
      "gtKommune": null,
      "gtBydel": "030101",
      "gtLand": null,
      "gtType": "BYDEL"
    },
    "hentPerson": {
      "adressebeskyttelse": []
    }
  },
  "extensions": {}
}
"""

@Language("JSON")
private const val gtLand = """
{
  "data": {
    "hentGeografiskTilknytning": {
      "gtKommune": null,
      "gtBydel": null,
      "gtLand": "SWE",
      "gtType": "UTLAND"
    },
    "hentPerson": {
      "adressebeskyttelse": []
    }
  },
  "extensions": {}
}
"""

@Language("JSON")
private const val ugradert = """
    {
      "data": {
        "hentGeografiskTilknytning": {
          "gtKommune": "0301",
          "gtBydel": null,
          "gtLand": null,
          "gtType": "KOMMUNE"
        },
        "hentPerson": {
          "adressebeskyttelse": [
            {
              "gradering": "UGRADERT"
            }
          ]
        }
      },
      "extensions": {}
    }
"""

@Language("JSON")
private const val ukjent = """
    {
      "data": {
        "hentGeografiskTilknytning": {
          "gtKommune": "0301",
          "gtBydel": null,
          "gtLand": null,
          "gtType": "KOMMUNE"
        },
        "hentPerson": {
          "adressebeskyttelse": []
        }
      },
      "extensions": {}
    }
"""


@Language("JSON")
private const val fortrolig = """
{
  "data": {
    "hentGeografiskTilknytning": {
      "gtKommune": "0301",
      "gtBydel": null,
      "gtLand": null,
      "gtType": "KOMMUNE"
    },
    "hentPerson": {
      "adressebeskyttelse": [
        {
          "gradering": "FORTROLIG"
        }
      ]
    }
  },
  "extensions": {}
}
"""

@Language("JSON")
private const val strengtFortrolig = """
    {
      "data": {
        "hentGeografiskTilknytning": {
          "gtKommune": "0301",
          "gtBydel": null,
          "gtLand": null,
          "gtType": "KOMMUNE"
        },
        "hentPerson": {
          "adressebeskyttelse": [
            {
              "gradering": "STRENGT_FORTROLIG"
            }
          ]
        }
      },
      "extensions": {}
    }
"""

@Language("JSON")
private const val strengtFortroligUtland = """
{
  "data": {
    "hentGeografiskTilknytning": {
      "gtKommune": "0301",
      "gtBydel": null,
      "gtLand": null,
      "gtType": "KOMMUNE"
    },
    "hentPerson": {
      "adressebeskyttelse": [
        {
          "gradering": "STRENGT_FORTROLIG_UTLAND"
        }
      ]
    }
  },
  "extensions": {}
}
"""
