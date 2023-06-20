plugins {
    kotlin("jvm") version "1.8.20"
    kotlin("plugin.serialization") version "1.8.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "net.bladehunt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.apache.commons:commons-configuration2:2.9.0")
    implementation("commons-beanutils:commons-beanutils:1.9.4")
    implementation("com.electronwill.night-config:toml:3.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
}
tasks.build {
    dependsOn("shadowJar")
}
tasks.test {
    useJUnitPlatform()
}
application {
    mainClass.set("net.bladehunt.installer.Main")
}
kotlin {
    jvmToolchain(8)
}