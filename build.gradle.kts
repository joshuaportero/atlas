import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java-library")
    id("checkstyle")
    id("com.gradleup.shadow") version "9.6.1"
    id("de.eldoria.plugin-yml.bukkit") version "0.9.0"
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

group = "dev.portero.atlas"
version = "0.0.1-DEV"

val liteCommandsVersion = "3.11.0"
val scoreboardLibraryVersion = "2.8.2"
val lombokVersion = "1.18.46"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
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
    mavenCentral()
    maven {
        name = "ExtendedClip"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        name = "FancyInnovations"
        url = uri("https://repo.fancyinnovations.com/releases")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    compileOnly("org.jetbrains:annotations:26.1.0")

    implementation("dev.rollczi:litecommands-bukkit:$liteCommandsVersion")
    implementation("dev.rollczi:litecommands-adventure:$liteCommandsVersion")

    implementation("net.megavex:scoreboard-library-api:$scoreboardLibraryVersion")
    runtimeOnly("net.megavex:scoreboard-library-implementation:$scoreboardLibraryVersion")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    implementation("com.zaxxer:HikariCP:7.1.0")
    implementation("org.postgresql:postgresql:42.7.13")
    implementation("org.xerial:sqlite-jdbc:3.53.2.1")
    implementation("com.mysql:mysql-connector-j:9.4.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("de.oliver:FancyHolograms:2.10.0")
}

bukkit {
    main = "dev.portero.atlas.AtlasPlugin"
    version = project.version.toString()
    apiVersion = "26.2"
    description = "Atlas is the core RPG plugin for quests, combat(skills), mmo, progression, and world events."
    website = "https://joshua.portero.dev/"
    authors = listOf("Portero")
    softDepend = listOf("PlaceholderAPI", "FancyHolograms")
    permissions {
        register("atlas.menu") {
            description = "Open the Atlas player menu"
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("atlas.admin") {
            description = "Open the Atlas admin menu"
            default = BukkitPluginDescription.Permission.Default.OP
        }
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation", "-parameters"))
    options.encoding = "UTF-8"
}

checkstyle {
    toolVersion = "13.8.0"

    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")

    maxErrors = 0
    maxWarnings = 0
}

configurations.named("checkstyle") {
    resolutionStrategy {
        capabilitiesResolution {
            withCapability("com.google.collections:google-collections") {
                select("com.google.guava:guava:33.7.1-jre")
            }
        }
    }
}

tasks.withType<ShadowJar> {
    dependsOn("checkstyleMain")

    relocate("dev.rollczi.litecommands", "dev.portero.atlas.libs.commands")
    relocate("net.megavex.scoreboardlibrary", "dev.portero.atlas.libs.scorelib")

    minimize {
        exclude(dependency("net.megavex:scoreboard-library-api"))
        exclude(dependency("net.megavex:scoreboard-library-implementation"))
        exclude(dependency("org.postgresql:postgresql"))
        exclude(dependency("org.xerial:sqlite-jdbc"))
        exclude(dependency("com.mysql:mysql-connector-j"))
    }

    archiveBaseName.set("Atlas-${project.version}")
    archiveVersion.set("")
    archiveClassifier.set("")
}

tasks {
    runServer {
        minecraftVersion("26.2")
    }
}
