plugins {
    id("com.github.johnrengelman.shadow")
    application
}

application {
    mainClass.set("personopplysninger.AppKt")
}

val aapLibsVersion = "3.1.14"
val ktorVersion = "2.0.3"

dependencies {
    implementation("com.github.navikt.aap-libs:ktor-client-auth:$aapLibsVersion")
    implementation("com.github.navikt.aap-libs:kafka:$aapLibsVersion")
    implementation("com.github.navikt.aap-libs:kafka-avroserde:$aapLibsVersion")
    implementation("com.github.navikt.aap-libs:ktor-utils:$aapLibsVersion")

    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")

    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:1.9.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")

    runtimeOnly("ch.qos.logback:logback-classic:1.2.11")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.2")

    testImplementation(kotlin("test"))
    testImplementation("com.github.navikt.aap-libs:kafka-test:$aapLibsVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}
