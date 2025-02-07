import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0-beta7"
    id("de.eldoria.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
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
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    compileOnly("org.jetbrains:annotations:24.0.0")

    implementation("com.github.retrooper:packetevents-spigot:2.7.0")

    implementation("dev.rollczi:litecommands-bukkit:3.9.6")
    implementation("dev.rollczi:litecommands-adventure:3.9.6")

    implementation("dev.triumphteam:triumph-gui:3.1.11")

    implementation("net.megavex:scoreboard-library-api:2.2.2")
    runtimeOnly("net.megavex:scoreboard-library-implementation:2.2.2")

    runtimeOnly("net.megavex:scoreboard-library-modern:2.2.2")

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.postgresql:postgresql:42.7.5")
}

bukkit {
    main = "dev.portero.atlas.AtlasPlugin"
    version = project.version.toString()
    apiVersion = "1.21.4"
    description = "Atlas is the core RPG plugin for quests, combat, progression, and world events."
    website = "https://joshua.portero.dev/"
    authors = listOf("Portero")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation", "-parameters"))
    options.encoding = "UTF-8"
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("Atlas-${project.version}")
    archiveVersion.set("")
    archiveClassifier.set("")

    relocate("dev.rollczi.litecommands", "dev.portero.atlas.libs.commands")
    relocate("dev.triumphteam.gui", "dev.portero.atlas.libs.gui")

    relocate("de.exlll.config", "dev.portero.atlas.libs.configuration")

    relocate("com.github.retrooper.packetevents", "dev.portero.atlas.libs.packetevents.api")
    relocate("io.github.retrooper.packetevents", "dev.portero.atlas.libs.packetevents.impl")

    relocate("net.megavex.scoreboardlibrary", "dev.portero.atlas.libs.scorelib")

    minimize {
        exclude(dependency("net.megavex:scoreboard-library-api"))
        exclude(dependency("net.megavex:scoreboard-library-implementation"))
        exclude(dependency("net.megavex:scoreboard-library-modern"))
        exclude(dependency("org.postgresql:postgresql"))
    }
}

tasks.withType<RunServer> {
    minecraftVersion("1.21.4")
}