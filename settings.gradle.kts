pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MobileAndroidIDE"

include(":app")
include(":core:model")
include(":feature:editor")
include(":feature:keyboard")
include(":feature:explorer")
include(":feature:diagnostics")
include(":feature:compiler")
include(":feature:ai")
