package personopplysninger

import no.nav.aap.kafka.streams.KStreamsConfig
import no.nav.aap.ktor.client.AzureConfig
import personopplysninger.norg.NorgConfig
import personopplysninger.pdl.api.PdlConfig


internal data class Config(
    val pdl: PdlConfig,
    val norg: NorgConfig,
    val azure: AzureConfig,
    val kafka: KStreamsConfig,
)
