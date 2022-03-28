import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    application
}

dependencies {
    implementation("io.ktor:ktor-server-netty:2.0.0-beta-1")
    implementation("io.ktor:ktor-server-core:2.0.0-beta-1")

    implementation("io.ktor:ktor-client-core:2.0.0-beta-1")
    implementation("io.ktor:ktor-client-cio:2.0.0-beta-1")
    implementation("io.ktor:ktor-client-content-negotiation:2.0.0-beta-1")

    implementation("io.ktor:ktor-serialization-jackson:2.0.0-beta-1")

//    implementation("io.ktor:ktor-client-jackson:2.0.0-beta-1")

    implementation("com.sksamuel.hoplite:hoplite-yaml:2.0.2")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.1")

    implementation(project(":libs:kafka"))

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:2.0.0-beta-1")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}

application {
    mainClass.set("norg.AppKt")
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDirs("main")
sourceSets["test"].resources.srcDirs("test")
