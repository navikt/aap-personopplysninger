plugins {
    id("com.github.johnrengelman.shadow")
    application
}

application {
    mainClass.set("personopplysninger.AppKt")
}

dependencies {
    implementation("com.github.navikt.aap-libs:ktor-client-auth:0.0.13")
    implementation("com.github.navikt.aap-libs:kafka:0.0.13")

    implementation("io.ktor:ktor-client-core:2.0.0")
    implementation("io.ktor:ktor-server-content-negotiation:2.0.0")
    implementation("io.ktor:ktor-server-core:2.0.0")
    implementation("io.ktor:ktor-server-netty:2.0.0")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:2.0.0")

    implementation("io.micrometer:micrometer-registry-prometheus:1.8.4")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2") // JavaTimeModule()
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.1.2")

    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.1.1")

    testImplementation(kotlin("test"))
    testImplementation("com.github.navikt.aap-libs:kafka-test:0.0.25")
    testImplementation("io.ktor:ktor-server-test-host:2.0.0")
    testImplementation("uk.org.webcompere:system-stubs-jupiter:2.0.1")
}
