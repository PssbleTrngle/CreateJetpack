plugins {
    id("com.possible-triangle.neoforge")
}

withKotlin()

neoforge {
    dataGen()
}

base {
    archivesName = "${mod.id.get()}-forge-${mod.version.get()}"
}

repositories {
    maven {
        url = uri("https://maven.blamejared.com/")
        content {
            includeGroup("mezz.jei")
        }
    }
    maven {
        url = uri("https://mvn.devos.one/snapshots")
        content {
            includeGroup("com.tterrag.registrate")
        }
    }
    maven {
        url = uri("https://maven.createmod.net")
        content {
            includeGroup("com.simibubi.create")
            includeGroup("net.createmod.ponder")
            includeGroup("dev.engine-room.flywheel")
        }
    }
    maven {
        url = uri("https://maven.theillusivec4.top/")
        content {
            includeGroup("com.illusivesoulworks.caelus")
            includeGroup("top.theillusivec4.curios")
        }
    }
    nexus {
        content {
            includeGroup("com.possible-triangle")
        }
    }
    maven {
        url = uri("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
        content {
            includeGroup("fuzs.forgeconfigapiport")
        }
    }
}

dependencies {
    modInclude(libs.flightlib.neoforge)

    modImplementation(libs.registrate)

    modImplementation(
        variantOf(libs.create) {
            classifier("slim")
        },
    ) {
        isTransitive = false
    }

    modImplementation(libs.ponder)
    modCompileOnly(libs.flywheel)

    if (!env.isCI) {
        modRuntimeOnly(libs.jei)

        // Only here to test jetpack+elytra combination behaviour
        modImplementation(libs.curios)
        modRuntimeOnly(libs.caelus)
        modRuntimeOnly(pack.modrinth.elytra.slot)
        modRuntimeOnly(pack.modrinth.cold.sweat)
    }

    modCompileOnly(libs.flightlib.api)
    modCompileOnly(libs.flightlib.neoforge.api)
}

tasks.withType<Jar> {
    exclude("screenshots")
}

upload {
    maven {
        nexus()
    }

    forEach {
        dependencies {
            required("create")
        }
    }

    modrinth {
        syncBodyFromReadme()
    }
}

enableSonarQube()
enableSpotless()
