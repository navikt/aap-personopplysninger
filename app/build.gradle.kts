plugins {
    id("com.github.johnrengelman.shadow")
    application
}

application {
    mainClass.set("personopplysninger.AppKt")
}

dependencies {
    implementation("com.github.navikt.aap-libs:ktor-client-auth:1.2.0")
    implementation("com.github.navikt.aap-libs:kafka:0.1.9")
    implementation("com.github.navikt.aap-libs:ktor-utils:0.1.9")

    implementation("io.ktor:ktor-client-logging:2.0.2")
    implementation("io.ktor:ktor-client-core:2.0.2")

    implementation("io.ktor:ktor-server-content-negotiation:2.0.2")
    implementation("io.ktor:ktor-server-core:2.0.2")
    implementation("io.ktor:ktor-server-netty:2.0.2")
    implementation("io.ktor:ktor-server-metrics-micrometer:2.0.2")

    implementation("io.micrometer:micrometer-registry-prometheus:1.9.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")

    runtimeOnly("ch.qos.logback:logback-classic:1.2.11")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.1.1")

    testImplementation(kotlin("test"))
    testImplementation("com.github.navikt.aap-libs:kafka-test:0.1.9")
    testImplementation("io.ktor:ktor-server-test-host:2.0.2")
}
