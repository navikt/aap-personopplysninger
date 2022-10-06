package personopplysninger.streams

import kotlinx.coroutines.runBlocking
import no.nav.aap.kafka.streams.extension.consume
import no.nav.aap.kafka.streams.extension.filterNotNull
import no.nav.aap.kafka.streams.extension.leftJoin
import no.nav.aap.kafka.streams.extension.produce
import no.nav.aap.kafka.streams.named
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Branched
import org.apache.kafka.streams.kstream.KTable
import org.slf4j.LoggerFactory
import personopplysninger.domain.Personopplysninger
import personopplysninger.domain.PersonopplysningerDto
import personopplysninger.graphql.PdlGraphQLClient
import personopplysninger.kafka.Topics
import personopplysninger.rest.ArbeidsfordelingDtoRequest
import personopplysninger.rest.NorgClient

private val secureLog = LoggerFactory.getLogger("secureLog")

internal fun StreamsBuilder.personopplysningStream(
    skjermede: KTable<String, SkjermetDto>,
    pdlClient: PdlGraphQLClient,
    norgClient: NorgClient,
) {
    consume(Topics.personopplysninger, true)
        .filterNotNull("skip-personopplysning-tombstone")
        .split(named("split"))
        .branch({ _, dto -> dto.kanSetteSkjerming() }, skjermingBranch(skjermede))
        .branch({ _, dto -> dto.kanSetteGraderingEllerGT() }, pdlBranch(pdlClient))
        .branch({ _, dto -> dto.kanSetteEnhet() }, norgBranch(norgClient))
}

internal fun skjermingBranch(skjermede: KTable<String, SkjermetDto>): Branched<String, PersonopplysningerDto> =
    Branched.withConsumer { stream ->
        stream
            .leftJoin(Topics.personopplysninger with Topics.skjerming, skjermede)
            .mapValues { (personopplysningerDto, skjermingDto) ->
                val personopplysninger = Personopplysninger.restore(personopplysningerDto)
                personopplysninger.settSkjerming(skjermingDto?.fom(), skjermingDto?.tom())
                personopplysninger.toDto()
            }
            .produce(Topics.personopplysninger, "produced-personopplysning-skjermet", true)
    }

internal fun pdlBranch(pdlClient: PdlGraphQLClient): Branched<String, PersonopplysningerDto> =
    Branched.withConsumer { stream ->
        stream
            .mapValues { personident, dto ->
                val personopplysninger = Personopplysninger.restore(dto)
                val response = runBlocking { pdlClient.hentAlt(personident) }
                if (response.errors != null) {
                    secureLog.warn("Feil fra PDL, ignorerer behov, ${response.errors}")
                    return@mapValues null
                }
                personopplysninger.settGeografiskTilknytning(response.geografiskTilknytning())
                personopplysninger.settAdressebeskyttelse(response.gradering())
                personopplysninger.toDto()
            }
            .filterNotNull("filter-not-null-personopplysning-pdl-error")
            .produce(Topics.personopplysninger, "produced-personopplysning-pdl", true)
    }

internal fun norgBranch(norgClient: NorgClient): Branched<String, PersonopplysningerDto> =
    Branched.withConsumer { stream ->
        stream
            .mapValues { dto ->
                val request = ArbeidsfordelingDtoRequest.create(dto)
                val response = runBlocking { norgClient.hentArbeidsfordeling(request) }.singleOrNull()
                val personopplysninger = Personopplysninger.restore(dto)
                personopplysninger.settEnhet(response?.enhetNr ?: "UKJENT")
                personopplysninger.toDto()
            }
            .produce(Topics.personopplysninger, "produced-personopplysning-enhet", true)
    }
