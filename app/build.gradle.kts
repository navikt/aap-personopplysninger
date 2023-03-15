import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    id("io.ktor.plugin") version "2.2.4"
}

application {
    mainClass.set("personopplysninger.AppKt")
}

val aapLibsVersion = "3.6.35"
val ktorVersion = "2.2.4"

dependencies {
    implementation("com.github.navikt.aap-libs:ktor-auth-azuread:$aapLibsVersion")
    implementation("com.github.navikt.aap-libs:kafka-2:$aapLibsVersion")
    implementation("com.github.navikt.aap-libs:ktor-utils:$aapLibsVersion")
    implementation("com.github.navikt.aap-libs:kafka-avroserde:$aapLibsVersion")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")

    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:1.10.5")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.2")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.5")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.3")

    testImplementation(kotlin("test"))
    testImplementation("com.github.navikt.aap-libs:kafka-test-2:$aapLibsVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    maven("https://packages.confluent.io/maven/")
}

configurations.all {
    resolutionStrategy {
        force("org.apache.kafka:kafka-clients:3.3.1")
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "19"
    }
    withType<Test> {
        useJUnitPlatform()
    }
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDirs("main")
sourceSets["test"].resources.srcDirs("test")