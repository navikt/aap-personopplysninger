package norg

import java.net.URL

data class Config(
    val norg: NorgConfig,
)

data class NorgConfig(
    val proxyUrl: URL,
)
