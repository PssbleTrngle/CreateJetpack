val mod_name: String by extra

pluginManagement {
    repositories {
        val env = System.getenv()

        gradlePluginPortal()
        maven { url = uri("https://maven.minecraftforge.net/") }
        maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }

        env["LOCAL_MAVEN"]?.let { localMaven ->
            maven { url = uri(localMaven) }
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
    }
}

rootProject.name = mod_name

