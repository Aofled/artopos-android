enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
include(":core:model")
include(":core:network")
include(":core:database")
include(":core:data")
include(":core:domain")
