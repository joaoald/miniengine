plugins {
    kotlin("jvm") version "2.4.0"
    id("com.gradleup.shadow") version "9.6.1"
}

group = "dev.akkih"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven(url = "https://repo.infernalsuite.com/repository/maven-snapshots/")
    maven(url = "https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    compileOnly("com.infernalsuite.asp:api:4.0.0-SNAPSHOT")
    compileOnly("com.infernalsuite.asp:file-loader:4.0.0-SNAPSHOT")
}

kotlin {
    jvmToolchain(25)
}