import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    application
}

dependencies {
    implementation("io.ktor:ktor-server-netty:1.6.8")
//    implementation("io.ktor:ktor-server-core:1.6.8")
    implementation("io.ktor:ktor-client-core:1.6.8")
    implementation("io.ktor:ktor-client-cio:1.6.8")
    implementation("io.ktor:ktor-client-jackson:1.6.8")
    implementation("io.ktor:ktor-jackson:1.6.8")
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.0.2")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.1")

    implementation("org.apache.kafka:kafka-clients:3.1.0")
    implementation("org.apache.kafka:kafka-streams:3.1.0")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:1.6.8")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}

application {
    mainClass.set("nom.skjerming.AppKt")
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDirs("main")
sourceSets["test"].resources.srcDirs("test")
