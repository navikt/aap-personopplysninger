plugins {
    id("com.github.johnrengelman.shadow")
    application
}

application {
    mainClass.set("personopplysninger.AppKt")
}

val aapLibsVersion = "3.4.7"
val ktorVersion = "2.1.2"

dependencies {
    implementation("com.github.navikt.aap-libs:ktor-auth-azuread:$aapLibsVersion")
    implementation("com.github.navikt.aap-libs:kafka:$aapLibsVersion")
//    implementation("com.github.navikt.aap-libs:kafka-avroserde:$aapLibsVersion")
    implementation("com.github.navikt.aap-libs:ktor-utils:$aapLibsVersion")

    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")

    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:1.9.4")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.4")

    runtimeOnly("ch.qos.logback:logback-classic:1.4.3")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.2")

    testImplementation(kotlin("test"))
    testImplementation("com.github.navikt.aap-libs:kafka-test:$aapLibsVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}
