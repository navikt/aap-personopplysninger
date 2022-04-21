plugins {
    kotlin("jvm")
}

dependencies {
    api("io.ktor:ktor-client-content-negotiation:2.0.0")
    api("io.ktor:ktor-serialization-jackson:2.0.0")
    api("io.ktor:ktor-client-cio:2.0.0")
    api("io.ktor:ktor-client-auth:2.0.0")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
