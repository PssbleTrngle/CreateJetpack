plugins {
    id("com.possible-triangle.forge")
}

withKotlin()

mod {
    mods.include(libs.flightlib.forge)
}

forge {
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
        url = uri("https://maven.tterrag.com/")
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
            includeGroupAndSubgroups("top.theillusivec4")
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
        modRuntimeOnly(libs.curios)
        modRuntimeOnly(libs.caelus)
        modRuntimeOnly(pack.modrinth.elytra.slot)
        modRuntimeOnly(pack.modrinth.cold.sweat)
    }

    modCompileOnly(libs.flightlib.api)
    modCompileOnly(libs.flightlib.forge.api)
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
