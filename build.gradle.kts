plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("com.gradleup.shadow") version "9.3.1"
    id("application")
}

application {
    mainClass.set("dev.blackoutburst.editor.MainKt")
}

group = "dev.blackoutburst.editor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("libs/Bogel-Reloaded.jar"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}
