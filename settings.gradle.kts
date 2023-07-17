val mod_name: String by extra

pluginManagement {
    repositories {
        val env = System.getenv()

        gradlePluginPortal()
        maven { url = uri("https://maven.minecraftforge.net/") }
        maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
    }
}

rootProject.name = mod_name