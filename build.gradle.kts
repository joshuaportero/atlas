import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("java-library")
    id("checkstyle")
    id("com.gradleup.shadow") version "9.5.1"
    id("de.eldoria.plugin-yml.bukkit") version "0.9.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "dev.portero.atlas"
version = "0.0.1-DEV"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
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
    maven {
        name = "EngineHub"
        url = uri("https://maven.enginehub.org/repo/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")

    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.4.5")

    compileOnly("org.jetbrains:annotations:26.1.0")

    implementation("dev.rollczi:litecommands-bukkit:3.11.0")
    implementation("dev.rollczi:litecommands-adventure:3.11.0")

    implementation("dev.triumphteam:triumph-gui:3.1.13")

    implementation("net.megavex:scoreboard-library-api:2.8.0")
    runtimeOnly("net.megavex:scoreboard-library-implementation:2.8.0")

    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")

    implementation("com.zaxxer:HikariCP:7.1.0")
    implementation("org.postgresql:postgresql:42.7.13")

    testCompileOnly("io.papermc.paper:paper-api:26.2.build.+")
    testRuntimeOnly("io.papermc.paper:paper-api:26.2.build.+")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")
    testImplementation("org.mockito:mockito-core:5.18.0")
}

bukkit {
    main = "dev.portero.atlas.AtlasPlugin"
    version = project.version.toString()
    apiVersion = "26.2"
    description = "Atlas is the core RPG plugin for quests, combat, progression, and world events."
    website = "https://joshua.portero.dev/"
    authors = listOf("Portero")
    softDepend = listOf("WorldEdit", "FastAsyncWorldEdit")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation", "-parameters"))
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

checkstyle {
    toolVersion = "13.8.0"

    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
    configProperties["checkstyle.suppressions.file"] = "${rootDir}/config/checkstyle/suppressions.xml"

    maxErrors = 0
    maxWarnings = 0
}

configurations.named("checkstyle") {
    resolutionStrategy {
        capabilitiesResolution {
            withCapability("com.google.collections:google-collections") {
                select("com.google.guava:guava:33.6.0-jre")
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
        exclude(dependency("org.postgresql:postgresql"))
    }

    archiveBaseName.set("Atlas-${project.version}")
    archiveVersion.set("")
    archiveClassifier.set("")
}

tasks.withType<RunServer> {
    minecraftVersion("26.2")
}