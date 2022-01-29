
plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("org.jlleitschuh.gradle.ktlint-idea") version "10.2.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}
