import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.gradleup.shadow") version "9.0.0-beta17"
}

group = "me.noahvdaa.healthider"
version = "1.0.8"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    implementation("org.bstats:bstats-bukkit:3.0.0")

    paperweight.paperDevBundle("1.21.6-R0.1-SNAPSHOT")
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(21)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    shadowJar {
        relocate("org.bstats", "me.noahvdaa.healthhider.libs.bstats")
    }

    runServer {
        minecraftVersion("1.21.6")
    }
}

bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "me.noahvdaa.healthhider.HealthHider"
    description = "HealthHider allows you to hide the health of other entities, to prevent players from gaining an unfair advantage."
    apiVersion = "1.20"
    authors = listOf("NoahvdAa")
    website = "https://github.com/NoahvdAa/HealthHider"
    foliaSupported = true
    permissions.create("healthider.bypass") {
        default = BukkitPluginDescription.Permission.Default.FALSE
    }
}
