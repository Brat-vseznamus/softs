import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:2.1.0")
    testImplementation("com.xebialabs.restito:restito:1.1.0")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")
    implementation("javax.servlet:javax.servlet-api:3.1.0")
    implementation("org.eclipse.jetty:jetty-servlet:9.4.21.v20190926")
    implementation("org.eclipse.jetty:jetty-server:9.4.21.v20190926")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}