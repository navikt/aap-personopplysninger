import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    application
}

dependencies {
    implementation("io.ktor:ktor-client-cio:2.0.0-beta-1")
    implementation("io.ktor:ktor-client-content-negotiation:2.0.1-eap-371")
    implementation("io.ktor:ktor-client-core:2.0.0-beta-1")
    implementation("io.ktor:ktor-serialization-jackson:2.0.0-beta-1")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.0.0-beta-1")
    implementation("io.ktor:ktor-server-core:2.0.0-beta-1")
    implementation("io.ktor:ktor-server-netty:2.0.0-beta-1")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.0.4")

    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:2.0.0-beta-1")
    implementation("io.micrometer:micrometer-registry-prometheus:1.8.4")

    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.0.1")

    implementation(project(":libs:kafka"))
    implementation(project(":libs:ktor-client-auth"))
    implementation(project(":models:personopplysninger"))

    testImplementation(kotlin("test"))
    testImplementation(project(":libs:kafka-test"))
    testImplementation("io.ktor:ktor-server-test-host:2.0.0-beta-1")
    testImplementation("uk.org.webcompere:system-stubs-jupiter:2.0.1")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}

application {
    mainClass.set("personopplysninger.AppKt")
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDirs("main")
sourceSets["test"].resources.srcDirs("test")
