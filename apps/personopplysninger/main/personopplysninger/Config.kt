package personopplysninger

import no.nav.aap.kafka.KafkaConfig
import pdl.api.AzureConfig
import personopplysninger.pdl.api.PdlConfig

internal data class Config(
    val pdl: PdlConfig,
    val azure: AzureConfig,
    val kafka: KafkaConfig,
)
