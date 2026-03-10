plugins {
    id("com.possible-triangle.fabric")
}

withKotlin()

mod {
    mods.include(libs.flightlib.fabric)
}

fabric {
    dataGen()
}

base {
    val rawVersion = mod.version.get().replace("-fabric", "")
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

    nexus {
        content {
            includeGroup("com.possible-triangle")
        }
    }
}

dependencies {
    modImplementation(libs.registrate)

    modImplementation(libs.create) {
        // exclude("com.jozufozu.flywheel")
    }

    modImplementation(libs.ponder)
    modImplementation(libs.flywheel)

    if (!env.isCI) {
        modRuntimeOnly(libs.jei)

        // Only here to test jetpack+elytra combination behaviour
        modRuntimeOnly(pack.modrinth.elytra.slot)
        modRuntimeOnly(pack.modrinth.trinkets)
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
