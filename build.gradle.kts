import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm").version(Kotlin.version)
    kotlin("plugin.serialization").version(Kotlin.version)

    application
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(DittNAV.Common.securityAuthenticatedUser)
    implementation(DittNAV.Common.utils)
    implementation(Ktor.auth)
    implementation(Ktor.authJwt)
    implementation(Ktor.clientApache)
    implementation(Ktor.clientJson)
    implementation(Ktor.clientSerializationJvm)
    implementation(Ktor.serverNetty)
    implementation(Ktor.serialization)
    implementation(Ktor.metricsMicrometer)
    implementation("io.github.microutils:kotlin-logging:2.1.23")
    implementation("com.github.navikt.tms-ktor-token-support:token-support-tokendings-exchange:2022.01.27-13.11-a6b55dd90347")
    implementation(Micrometer.registryPrometheus)

    testImplementation(kotlin("test"))
    testImplementation(Kotest.assertionsCore)
    testImplementation(Kotest.runnerJunit5)
    testImplementation(Ktor.clientMock)
    testImplementation(Ktor.serverTestHost)
    testImplementation(Mockk.mockk)
}

application {
    mainClass.set("no.nav.tms.varselbjelle.api.ApplicationKt")
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            showExceptions = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }
}