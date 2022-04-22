package personopplysninger.pdl.streams

import kotlinx.coroutines.runBlocking
import personopplysninger.Personopplysninger
import no.nav.aap.kafka.streams.produce
import org.apache.kafka.streams.kstream.KStream
import personopplysninger.Topics
import personopplysninger.pdl.api.PdlData
import personopplysninger.pdl.api.PdlGraphQLClient

internal fun pdlStream(pdlClient: PdlGraphQLClient) = { chain: KStream<String, Personopplysninger> ->
    chain
        .mapValues { personident, personopplysninger ->
            val response = runBlocking { pdlClient.hentAlt(personident) }
            personopplysninger.apply {
                personopplysninger.settAdressebeskyttelse(response.adressebeskyttelse?.gradering)
                personopplysninger.settGeografiskTilknytning(response.geografiskTilknytning)
            }
        }
        .mapValues(Personopplysninger::toDto)
        .produce(Topics.personopplysninger) { "produced-personopplysning-pdl" }
}

private fun Personopplysninger.settGeografiskTilknytning(gt: PdlData.GeografiskTilknytning) {
    gt.gtBydel?.let(::settTilhørendeBydel)
        ?: gt.gtKommune?.let(::settTilhørendeKommune)
        ?: gt.gtLand?.let(::settTilhørendeLand)
        ?: error("skal denne være 'uten fast bopel'") // TODO: hør med PDL
}
