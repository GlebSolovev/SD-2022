import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration

plugins {
    kotlin("jvm") version "1.6.10"

    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("org.jlleitschuh.gradle.ktlint-idea") version "10.2.1"

    id("io.gitlab.arturbosch.detekt") version "1.19.0"

    id("org.jetbrains.dokka") version "1.6.10"
}

buildscript {
    // Import the scripts defining the `DokkaBaseConfiguration` class and the like.
    // This is to be able to configure the HTML Dokka plugin (custom styles, etc.)
    // Note: this can't be put in buildSrc unfortunately
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.6.10")
    }
}

group = "ru.hse.ezh"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.buildobjects:jproc:2.8.0")

    testImplementation(kotlin("test"))

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.6.0")
}

tasks.test {
    useJUnitPlatform()
}

detekt {
    buildUponDefaultConfig = true
    config = files("$projectDir/config/detekt.yml")
}

tasks.dokkaHtml.configure {
    dokkaSourceSets {
        pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
            customAssets = listOf(file("logo.png"))
            customStyleSheets = listOf(file("config/dokka/logo-styles.css"))
        }
    }
}

tasks.register("lint") {
    dependsOn("detekt", "ktlintCheck")
}
