package personopplysninger.kafka

import no.nav.aap.kafka.streams.v2.Topic
import no.nav.aap.kafka.streams.v2.serde.ByteArraySerde
import no.nav.aap.kafka.streams.v2.serde.JsonSerde
import no.nav.aap.kafka.streams.v2.serde.StringSerde
import personopplysninger.aktor.AktorDto
import personopplysninger.domain.PersonopplysningerDto
import personopplysninger.domain.PersonopplysningerInternDto
import personopplysninger.streams.GtMedIdenter
import personopplysninger.streams.SkjermetDto

object Topics {
    //    val leesah = Topic("aapen-person-pdl-leesah-v1", AvroSerde.generic())
    val skjerming = Topic("nom.skjermede-personer-v1", JsonSerde.jackson<SkjermetDto>())
    val personopplysninger = Topic("aap.personopplysninger.v1", JsonSerde.jackson<PersonopplysningerDto>(), logValues = true)
    val personopplysningerIntern = Topic("aap.personopplysninger-intern.v1", JsonSerde.jackson<PersonopplysningerInternDto>(), logValues = true)
    val søknad = Topic("aap.soknad-sendt.v1", ByteArraySerde)
    val søkere = Topic("aap.sokere.v1", ByteArraySerde)
    val geografiskTilknytning = Topic("aapen-pdl-geografisktilknytning-v1", JsonSerde.jackson<GtMedIdenter>())

    val aktørV2: Topic<AktorDto> = Topic("pdl.aktor-v2", AktorAvroSerde(), logValues = true)
    val endredePersonidenter = Topic("aap.endrede-personidenter.v1", StringSerde)
}
