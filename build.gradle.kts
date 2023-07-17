val mod_id: String by extra
val mc_version: String by extra
val registrate_version: String by extra
val create_version: String by extra
val flywheel_version: String by extra
val flightlib_version: String by extra
val mod_version: String by extra
val curios_version: String by extra
val caelus_version: String by extra
val elytra_slot_version: String by extra
val jei_version: String by extra

plugins {
    id("net.somethingcatchy.gradle") version("0.0.3")
}

withKotlin()

forge {
    dataGen()
    includesMod("com.possible_triangle:flightlib-forge:${flightlib_version}")
}

base {
    archivesName.set("$mod_id-forge-$mod_version")
}

repositories {
    curseMaven()

    maven {
        url = uri("https://maven.blamejared.com/")
        content {
            includeGroup("mezz.jei")
        }
    }
    maven {
        url = uri("https://maven.tterrag.com/")
        content {
            includeGroup("com.simibubi.create")
            includeGroup("com.jozufozu.flywheel")
            includeGroup("com.tterrag.registrate")
        }
    }
    maven {
        url = uri("https://maven.theillusivec4.top/")
        content {
            includeGroup("top.theillusivec4.caelus")
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
            includeGroup("com.possible_triangle")
        }
    }
}

dependencies {
    modImplementation("com.tterrag.registrate:Registrate:${registrate_version}")
    modImplementation("com.simibubi.create:create-${mc_version}:${create_version}:slim") { isTransitive = false }
    modImplementation("com.jozufozu.flywheel:flywheel-forge-${mc_version}:${flywheel_version}")

    if (!env.isCI) {
        modRuntimeOnly("mezz.jei:jei-${mc_version}:${jei_version}")

        // Only here to test jetpack+elytra combination behaviour
        modRuntimeOnly("top.theillusivec4.curios:curios-forge:${curios_version}")
        modRuntimeOnly("top.theillusivec4.caelus:caelus-forge:${caelus_version}")
        modRuntimeOnly("curse.maven:elytra-slot-317716:${elytra_slot_version}")
    }

    modCompileOnly("com.possible_triangle:flightlib-api:${flightlib_version}")
    modCompileOnly("com.possible_triangle:flightlib-forge-api:${flightlib_version}")
}

tasks.withType<Jar> {
    exclude("screenshots")
}

enablePublishing {
    repositories {
        githubPackages(project)
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