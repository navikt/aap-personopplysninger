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
import personopplysninger.domain.PersonopplysningerInternDto
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
    consume(Topics.personopplysningerIntern, true)
        .filterNotNull("skip-personopplysning-tombstone")
        .split(named("split"))
        .branch({ _, dto -> dto.kanSetteSkjerming() }, skjermingBranch(skjermede))
        .branch({ _, dto -> dto.kanSettePdlopplysninger() }, pdlBranch(pdlClient))
        .branch({ _, dto -> dto.kanSetteEnhet() }, norgBranch(norgClient))
        .defaultBranch(ferdigBranch())
}

internal fun skjermingBranch(skjermede: KTable<String, SkjermetDto>): Branched<String, PersonopplysningerInternDto> =
    Branched.withConsumer { stream ->
        stream
            .leftJoin(Topics.personopplysningerIntern with Topics.skjerming, skjermede)
            .mapValues { (personopplysningerDto, skjermingDto) ->
                val personopplysninger = Personopplysninger.restore(personopplysningerDto)
                personopplysninger.settSkjerming(skjermingDto?.fom(), skjermingDto?.tom())
                personopplysninger.toDto()
            }
            .produce(Topics.personopplysningerIntern, "produced-personopplysning-skjermet", true)
    }

internal fun pdlBranch(pdlClient: PdlGraphQLClient): Branched<String, PersonopplysningerInternDto> =
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
                val navn = requireNotNull(response.data?.hentPerson?.navn?.last()) { "Fant ikke navn i PDL" }
                personopplysninger.settNavn(navn.fornavn, navn.mellomnavn, navn.etternavn)
                personopplysninger.toDto()
            }
            .filterNotNull("filter-not-null-personopplysning-pdl-error")
            .produce(Topics.personopplysningerIntern, "produced-personopplysning-pdl", true)
    }

internal fun norgBranch(norgClient: NorgClient): Branched<String, PersonopplysningerInternDto> =
    Branched.withConsumer { stream ->
        stream
            .mapValues { personident, dto ->
                val request = ArbeidsfordelingDtoRequest.create(dto)
                val response = runBlocking { norgClient.hentArbeidsfordeling(request) }.singleOrNull()
                if (response == null) {
                    secureLog.warn("Tom enhetsliste fra norg på $personident (${dto})")
                    return@mapValues null
                }
                val personopplysninger = Personopplysninger.restore(dto)
                personopplysninger.settEnhet(response.enhetNr)
                personopplysninger.toDto()
            }
            .filterNotNull("filter-not-null-personopplysning-norg-liste")
            .produce(Topics.personopplysningerIntern, "produced-personopplysning-enhet", true)
    }

internal fun ferdigBranch(): Branched<String, PersonopplysningerInternDto> =
    Branched.withConsumer { stream ->
        stream
            .mapValues { _, value -> value.mapTilPersonopplysningerDto() }
            .produce(Topics.personopplysninger, "produced-personopplysninger-finished", true)
    }
