package nom.skjerming

import no.nav.aap.kafka.serde.JsonSerde
import no.nav.aap.kafka.streams.Table
import no.nav.aap.kafka.streams.Topic
import nom.skjerming.skjerming.SkjermetPersonDto
import org.apache.kafka.common.serialization.Serdes

object Tables {
    val skjerming = Table("skjerming", Topics.skjerming)
}

object Topics {
    val skjerming = Topic("nom.skjermede-personer-v1", Serdes.StringSerde(), JsonSerde.create<SkjermetPersonDto>())
}
