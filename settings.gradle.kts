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
include(":core:model")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":core:domain")
include(":core:designsystem")
include(":core:ui-components")
include(":core:common")
include(":core:navigation")
include(":core:translation")
include(":feature:artwork-card")
include(":feature:discover")
include(":feature:favorites")
include(":feature:details")
include(":feature:settings")
