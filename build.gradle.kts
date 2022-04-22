import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://packages.confluent.io/maven/") // transitive avro dependency
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks {
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "18"
        }
        withType<Test> {
            useJUnitPlatform()
        }
    }

    kotlin.sourceSets["main"].kotlin.srcDirs("main")
    kotlin.sourceSets["test"].kotlin.srcDirs("test")
    sourceSets["main"].resources.srcDirs("main")
    sourceSets["test"].resources.srcDirs("test")
}
