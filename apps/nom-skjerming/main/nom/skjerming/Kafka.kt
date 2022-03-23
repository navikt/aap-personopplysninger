package nom.skjerming

import no.nav.aap.kafka.JsonSerde
import no.nav.aap.kafka.Table
import no.nav.aap.kafka.Topic
import nom.skjerming.skjerming.SkjermetPersonDto
import org.apache.kafka.common.serialization.Serdes

object Tables {
    val skjerming = Table("skjerming", Topics.skjerming)
}

object Topics {
    val skjerming = Topic("nom.skjermede-personer-v1", Serdes.StringSerde(), JsonSerde.create<SkjermetPersonDto>())
}
