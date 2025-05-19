val mod_id: String by extra
val mc_version: String by extra
val registrate_version: String by extra
val create_version: String by extra
val ponder_version: String by extra
val flywheel_version: String by extra
val flightlib_version: String by extra
val mod_version: String by extra
val curios_version: String by extra
val caelus_version: String by extra
val elytra_slot_version: String by extra
val jei_version: String by extra

plugins {
    id("com.possible-triangle.gradle") version ("0.2.8")
}

withKotlin()

neoforge {
    dataGen()
    includesMod("com.possible-triangle:flightlib-forge:${flightlib_version}")
}

base {
    archivesName.set("$mod_id-forge-$mod_version")
}

repositories {
    modrinthMaven()
    mavenLocal()

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
    maven {
        url = uri("https://maven.pkg.github.com/PssbleTrngle/FlightLib")
        credentials {
            username = env["GITHUB_ACTOR"]
            password = env["GITHUB_TOKEN"]
        }
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
    modImplementation("com.tterrag.registrate:Registrate:${registrate_version}")
    modImplementation("com.simibubi.create:create-${mc_version}:${create_version}:slim") { isTransitive = false }
    modImplementation("net.createmod.ponder:Ponder-NeoForge-${mc_version}:${ponder_version}")
    modCompileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-${mc_version}:${flywheel_version}")
    modRuntimeOnly("dev.engine-room.flywheel:flywheel-neoforge-${mc_version}:${flywheel_version}")

    if (!env.isCI) {
        modRuntimeOnly("mezz.jei:jei-${mc_version}-neoforge:${jei_version}")

        // Only here to test jetpack+elytra combination behaviour
        modImplementation("top.theillusivec4.curios:curios-neoforge:${curios_version}+${mc_version}")
        modRuntimeOnly("com.illusivesoulworks.caelus:caelus-neoforge:${caelus_version}+${mc_version}")
        modRuntimeOnly("maven.modrinth:mSQF1NpT:${elytra_slot_version}")
    }

    modCompileOnly("com.possible-triangle:flightlib-api:${flightlib_version}")
    modCompileOnly("com.possible-triangle:flightlib-forge-api:${flightlib_version}")
}

tasks.withType<Jar> {
    exclude("screenshots")
}

enablePublishing {
    githubPackages()
    repositories {
        mavenLocal()
    }
}

uploadToCurseforge {
    dependencies {
        required("create")
    }
}

uploadToModrinth {
    dependencies {
        required("LNytGWDc")
    }

    syncBodyFromReadme()
}

enableSonarQube()
enableSpotless()