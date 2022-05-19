package personopplysninger.pdl.streams

import kotlinx.coroutines.runBlocking
import no.nav.aap.kafka.streams.produce
import org.apache.kafka.streams.kstream.KStream
import personopplysninger.Personopplysninger
import personopplysninger.Topics
import personopplysninger.pdl.api.PdlGraphQLClient
import personopplysninger.pdl.api.PdlResponse

internal fun pdlStream(pdlClient: PdlGraphQLClient) = { chain: KStream<String, Personopplysninger> ->
    chain
        .mapValues { personident, personopplysninger ->
            val response = runBlocking { pdlClient.hentAlt(personident) }
            personopplysninger.apply {
                settAdressebeskyttelse(response.gradering())
                settGeografiskTilknytning(response.geografiskTilknytning())
            }
        }
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
