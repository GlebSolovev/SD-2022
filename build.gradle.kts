import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration

plugins {
    kotlin("jvm") version "1.6.10"

    application

    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("org.jlleitschuh.gradle.ktlint-idea") version "10.2.1"

    id("io.gitlab.arturbosch.detekt") version "1.19.0"

    id("org.jetbrains.dokka") version "1.6.10"

    id("com.adarshr.test-logger") version "3.1.0"

    jacoco

    id("org.barfuin.gradle.jacocolog") version "2.0.0"
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

    implementation("com.github.ajalt.clikt:clikt:3.4.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

application {
    mainClass.set("ru.hse.ezh.EzhKt")
}

tasks.withType<Jar> { // build proper .jar will all dependencies
    manifest {
        attributes["Main-Class"] = "ru.hse.ezh.EzhKt"
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
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

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {

    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        html.required.set(true)
        xml.required.set(false)
        csv.required.set(false)
    }

    finalizedBy("jacocoTestCoverageVerification")

}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.9".toBigDecimal()
            }
        }
    }
}
