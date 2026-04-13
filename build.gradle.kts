plugins {
    id("com.possible-triangle.fabric")
}

withKotlin()

fabric {
    dataGen()
}

val rawVersion = mod.version.get().replace("-fabric", "")
base {
    archivesName = "${mod.id.get()}-fabric-$rawVersion"
}

repositories {
    maven {
        url = uri("https://mvn.devos.one/snapshots/")
        content {
            includeGroup("com.simibubi.create")
            includeGroup("io.github.tropheusj")
            includeGroup("com.tterrag.registrate_fabric")
        }
    }

    maven {
        url = uri("https://maven.createmod.net")
        content {
            includeGroup("net.createmod.ponder")
            includeGroup("dev.engine-room.flywheel")
        }
    }

    maven {
        url = uri("https://mvn.devos.one/releases/")
        content {
            includeGroup("io.github.fabricators_of_create.Porting-Lib")
        }
    }

    maven {
        url = uri("https://maven.jamieswhiteshirt.com/libs-release")
        content {
            includeGroup("com.jamieswhiteshirt")
        }
    }

    maven {
        url = uri("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
        content {
            includeGroup("net.minecraftforge")
            includeGroup("fuzs.forgeconfigapiport")
        }
    }

    maven {
        url = uri("https://maven.blamejared.com/")
        content {
            includeGroup("mezz.jei")
        }
    }

    maven {
        url = uri("https://maven.terraformersmc.com/")
        content {
            includeGroup("dev.emi")
        }
    }

    maven {
        url = uri("https://maven.ladysnake.org/releases")
        content {
            includeGroup("dev.onyxstudios.cardinal-components-api")
        }
    }

    nexus {
        content {
            includeGroup("com.possible-triangle")
        }
    }
}

dependencies {
    modInclude(libs.flightlib.fabric)

    modImplementation(libs.registrate)
    modImplementation(libs.create)

    if (!env.isCI) {
        modRuntimeOnly(libs.jei)

        // Only here to test jetpack+elytra combination behaviour
        modRuntimeOnly(pack.modrinth.elytra.slot)
        modRuntimeOnly(libs.trinkets)
    }

    modCompileOnly(libs.flightlib.api)
}

tasks.withType<Jar> {
    exclude("screenshots")
}

upload {
    maven {
        nexus()
    }

    forEach {
        versionName = "Fabric $rawVersion"
        dependencies {
            required("create-fabric")
        }
    }

    modrinth {
        syncBodyFromReadme()
    }
}

enableSonarQube()
enableSpotless()
