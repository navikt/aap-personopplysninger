package personopplysninger.pdl.streams

import kotlinx.coroutines.runBlocking
import model.Personopplysninger
import no.nav.aap.kafka.streams.produce
import org.apache.kafka.streams.kstream.KStream
import personopplysninger.Topics
import personopplysninger.pdl.api.PdlGraphQLClient

internal fun pdlStream(pdlClient: PdlGraphQLClient) = { chain: KStream<String, Personopplysninger> ->
    chain
        .mapValues { personident, personopplysninger ->
            // fixme: dette kan gjøres penere
            personopplysninger.apply {
                val response = runBlocking { pdlClient.hentAlt(personident) }
                settAdressebeskyttelse(response.adressebeskyttelse.gradering)
                response.geografiskTilknytning.let { gt ->
                    gt.gtBydel?.let { settTilhørendeBydel(it) }
                        ?: gt.gtKommune?.let { settTilhørendeKommune(it) }
                        ?: gt.gtLand?.let { settTilhørendeLand(it) }
                        ?: error("skal denne være 'uten fast bopel'") // TODO: hør med PDL
                }
            }
        }
        .produce(Topics.personopplysninger) { "produced-personopplysning-skjermet" }
}
