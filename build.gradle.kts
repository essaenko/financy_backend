import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
val ktor_core_version = "2.0.2"
val ktor_deps_version = "1.6.8"
val prometheus_version = "1.9.0"
val ktorm_version = "3.5.0"
val ktor_serializer_version = "1.3.3"
val logback_version = "1.2.11"

plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.31"
    application
}

group = "me.essaenko"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-server-core:$ktor_core_version")
    implementation("io.ktor:ktor-server-netty:$ktor_core_version")
    implementation("io.ktor:ktor-server-cors:$ktor_core_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_core_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_core_version")
    implementation("io.ktor:ktor-server-auth:$ktor_core_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_core_version")
    implementation("io.ktor:ktor-metrics-micrometer:$ktor_deps_version")

    implementation("io.ktor:ktor-server-metrics-micrometer:$ktor_core_version")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheus_version")

    implementation("org.ktorm:ktorm-core:$ktorm_version")
    implementation("org.ktorm:ktorm-support-mysql:$ktorm_version")

    implementation(files("lib/mysql-connector-java-8.0.27.jar"))
    implementation("ch.qos.logback:logback-classic:$logback_version")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("com.financy.ApplicationKt")
}