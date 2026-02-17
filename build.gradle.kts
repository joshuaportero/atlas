import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("java-library")
    id("checkstyle")
    id("com.gradleup.shadow") version "9.3.1"
    id("de.eldoria.plugin-yml.bukkit") version "0.8.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "dev.portero.atlas"
version = "0.0.1-DEV"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "Panda-Lang"
        url = uri("https://repo.panda-lang.org/releases")
    }
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.io/repository/maven-releases/")
    }
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    compileOnly("org.jetbrains:annotations:26.0.2")

    implementation("dev.rollczi:litecommands-bukkit:3.10.9")
    implementation("dev.rollczi:litecommands-adventure:3.10.9")

    implementation("dev.triumphteam:triumph-gui:3.1.13")

    implementation("net.megavex:scoreboard-library-api:2.4.4")
    runtimeOnly("net.megavex:scoreboard-library-implementation:2.4.4")

    runtimeOnly("net.megavex:scoreboard-library-modern:2.4.4")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
}

bukkit {
    main = "dev.portero.atlas.AtlasPlugin"
    version = project.version.toString()
    name = "Atlas"
    apiVersion = "1.21.11"
    description = "Atlas is the core RPG plugin for quests, combat, progression, and world events."
    website = "https://joshua.portero.dev/"
    authors = listOf("Portero")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation", "-parameters"))
    options.encoding = "UTF-8"
}

checkstyle {
    toolVersion = "13.2.0"

    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
    configProperties["checkstyle.suppressions.file"] = "${rootDir}/config/checkstyle/suppressions.xml"

    maxErrors = 0
    maxWarnings = 0
}

configurations.named("checkstyle") {
    resolutionStrategy {
        capabilitiesResolution {
            withCapability("com.google.collections:google-collections") {
                select("com.google.guava:guava:33.5.0-jre")
            }
        }
    }
}

tasks.withType<ShadowJar> {
    dependsOn("checkstyleMain")

    relocate("dev.rollczi.litecommands", "dev.portero.atlas.libs.commands")
    relocate("dev.triumphteam.gui", "dev.portero.atlas.libs.gui")

    relocate("de.exlll.config", "dev.portero.atlas.libs.configuration")

    relocate("net.megavex.scoreboardlibrary", "dev.portero.atlas.libs.scorelib")

    minimize {
        exclude(dependency("net.megavex:scoreboard-library-api"))
        exclude(dependency("net.megavex:scoreboard-library-implementation"))
        exclude(dependency("net.megavex:scoreboard-library-modern"))
    }

    archiveBaseName.set("Atlas-${project.version}")
    archiveVersion.set("")
    archiveClassifier.set("")
}

tasks.withType<RunServer> {
    minecraftVersion("1.21.11")
}