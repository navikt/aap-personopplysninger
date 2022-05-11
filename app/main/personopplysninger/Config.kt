package personopplysninger

import no.nav.aap.kafka.KafkaConfig
import no.nav.aap.ktor.client.AzureConfig
import personopplysninger.norg.ProxyConfig
import personopplysninger.pdl.api.PdlConfig


internal data class Config(
    val pdl: PdlConfig,
    val proxy: ProxyConfig,
    val azure: AzureConfig,
    val kafka: KafkaConfig,
)
