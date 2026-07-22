enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS") // Enables syntax: projects.feature.home (instead of project(":feature:home"))

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

includeBuild("build-logic")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// ./gradlew :core:model:assembleDebug Compile specific module (e.g. core:model):
rootProject.name = "Artopos"
include(":app")
include(":benchmark")
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:domain")
include(":core:image-loader")
include(":core:model")
include(":core:navigation")
include(":core:network")
include(":core:translation")
include(":core:ui-components")
include(":core:artwork-card")
include(":feature:details")
include(":feature:discover")
include(":feature:favorites")
include(":feature:settings")
