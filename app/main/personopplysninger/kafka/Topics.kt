package personopplysninger.kafka

import no.nav.aap.kafka.serde.json.JsonSerde
import no.nav.aap.kafka.streams.Topic
import org.apache.kafka.common.serialization.Serdes.ByteArraySerde
import org.apache.kafka.common.serialization.Serdes.StringSerde
import personopplysninger.aktor.AktorDto
import personopplysninger.domain.PersonopplysningerDto
import personopplysninger.domain.PersonopplysningerInternDto
import personopplysninger.streams.GtMedIdenter
import personopplysninger.streams.SkjermetDto

object Topics {
    //    val leesah = Topic("aapen-person-pdl-leesah-v1", AvroSerde.generic())
    val skjerming = Topic("nom.skjermede-personer-v1", JsonSerde.jackson<SkjermetDto>())
    val personopplysninger = Topic("aap.personopplysninger.v1", JsonSerde.jackson<PersonopplysningerDto>())
    val personopplysningerIntern = Topic("aap.personopplysninger-intern.v1", JsonSerde.jackson<PersonopplysningerInternDto>())
    val søknad = Topic("aap.soknad-sendt.v1", ByteArraySerde())
    val søkere = Topic("aap.sokere.v1", ByteArraySerde())
    val geografiskTilknytning = Topic("aapen-pdl-geografisktilknytning-v1", JsonSerde.jackson<GtMedIdenter>())

    val aktørV2: Topic<AktorDto> = Topic("pdl.aktor-v2", AktorAvroSerde())
    val endredePersonidenter = Topic("aap.endrede-personidenter.v1", StringSerde())
}
