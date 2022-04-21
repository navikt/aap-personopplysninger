plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    application
}

dependencies {
    implementation("io.ktor:ktor-client-cio:2.0.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.0.0")
    implementation("io.ktor:ktor-client-core:2.0.0")
    implementation("io.ktor:ktor-serialization-jackson:2.0.0")
    implementation("io.ktor:ktor-server-content-negotiation:2.0.0")
    implementation("io.ktor:ktor-server-core:2.0.0")
    implementation("io.ktor:ktor-server-netty:2.0.0")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.1.2")

    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:2.0.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.8.4")

    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.1.1")


    implementation("com.github.navikt.aap-libs:kafka:0.0.12")
    implementation("com.github.navikt.aap-libs:ktor-client-auth:0.0.9")

    implementation("io.ktor:ktor-client-auth:2.0.0")
    implementation("org.apache.kafka:kafka-streams:3.1.0")
    implementation("io.confluent:kafka-streams-avro-serde:7.0.1") {
        exclude("org.apache.kafka", "kafka-clients")
    }

    implementation(project(":models:personopplysninger"))

    testImplementation(kotlin("test"))

    testImplementation("com.github.navikt.aap-libs:kafka-test:0.0.9")

    testImplementation("io.ktor:ktor-server-test-host:2.0.0")
    testImplementation("uk.org.webcompere:system-stubs-jupiter:2.0.1")
}

application {
    mainClass.set("personopplysninger.AppKt")
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDirs("main")
sourceSets["test"].resources.srcDirs("test")
