pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "app_laser_biatlon"
include(":app")
include(":navigation")
include(":database")
include(":core_common")
include(":core_network")
include(":core_ui")
include(":feature_connection_api")
include(":feature_connection_impl")
include(":feature_statistics_api")
include(":feature_statistics_impl")
include(":feature_targets_api")
include(":feature_targets_impl")
