package personopplysninger.pdl.streams

import kotlinx.coroutines.runBlocking
import no.nav.aap.kafka.streams.extension.filterNotNull
import no.nav.aap.kafka.streams.extension.produce
import org.apache.kafka.streams.kstream.KStream
import org.slf4j.LoggerFactory
import personopplysninger.Personopplysninger
import personopplysninger.Topics
import personopplysninger.pdl.api.PdlGraphQLClient
import personopplysninger.pdl.api.PdlResponse

private val secureLog = LoggerFactory.getLogger("secureLog")

internal fun pdlStream(pdlClient: PdlGraphQLClient) = { chain: KStream<String, Personopplysninger> ->
    chain
        .mapValues { personident, personopplysninger ->
            val response = runBlocking { pdlClient.hentAlt(personident) }
            if (response.errors != null) {
                secureLog.warn("Feil fra PDL, ignorerer behov, ${response.errors}")
                return@mapValues null
            }
            val navn = response.data?.hentPerson?.navn?.last() ?: error("fant ikke påkrevd navn")
            personopplysninger.apply {
                settAdressebeskyttelse(response.gradering())
                settGeografiskTilknytning(response.geografiskTilknytning())
                settNavn(
                        fornavn = navn.fornavn,
                        etternavn = navn.etternavn,
                        mellomnavn = navn.mellomnavn,
                )
            }
        }
        .filterNotNull("filter-not-null-personopplysning-pdl-error")
        .mapValues(Personopplysninger::toDto)
        .produce(Topics.personopplysninger, "produced-personopplysning-pdl")
}

private fun PdlResponse.gradering(): String =
    data?.hentPerson?.adressebeskyttelse?.singleOrNull()?.gradering ?: "UGRADERT"

private fun PdlResponse.geografiskTilknytning(): String =
    data?.hentGeografiskTilknytning?.gtBydel
        ?: data?.hentGeografiskTilknytning?.gtKommune
        ?: data?.hentGeografiskTilknytning?.gtLand
        ?: data?.hentGeografiskTilknytning?.gtType
        ?: "UKJENT"
