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
    implementation(DittNAV.Common.utils)
    implementation(Ktor2.Client.core)
    implementation(Ktor2.Client.apache)
    implementation(Ktor2.Client.contentNegotiation)
    implementation(Ktor2.Server.core)
    implementation(Ktor2.Server.netty)
    implementation(Ktor2.Server.auth)
    implementation(Ktor2.Server.contentNegotiation)
    implementation(Ktor2.Server.statusPages)
    implementation(Ktor2.Server.metricsMicrometer)
    implementation(Ktor2.Server.defaultHeaders)
    implementation(Ktor2.Server.cors)
    implementation(Ktor2.TmsTokenSupport.azureValidation)
    implementation(Ktor2.TmsTokenSupport.azureExchange)
    implementation(KotlinLogging.logging)
    implementation(Logstash.logbackEncoder)
    implementation("io.ktor:ktor-server-call-logging:2.1.1")
    implementation(Ktor2.kotlinX)

    implementation(Micrometer.registryPrometheus)

    testImplementation(kotlin("test"))
    testImplementation(Kotest.assertionsCore)
    testImplementation(Kotest.runnerJunit5)
    testImplementation(Ktor2.Test.serverTestHost)
    testImplementation(Ktor2.TmsTokenSupport.azureValidationMock)
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
