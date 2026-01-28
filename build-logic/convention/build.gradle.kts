plugins {
    `kotlin-dsl`
    alias(libs.plugins.detekt)
}

group = "ru.createsmart.artopos.buildlogic"

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)

    implementation(libs.detekt.gradlePlugin)
    detektPlugins(libs.detekt.formatting)
}

detekt {
    config.setFrom(files("../../config/detekt/detekt.yml"))
    autoCorrect = true
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "artopos.android.application"
            implementationClass = "ru.createsmart.artopos.AndroidApplicationConventionPlugin"
        }

        register("androidLibrary") {
            id = "artopos.android.library"
            implementationClass = "ru.createsmart.artopos.AndroidLibraryConventionPlugin"
        }

        register("androidApplicationCompose") {
            id = "artopos.android.application.compose"
            implementationClass = "ru.createsmart.artopos.AndroidApplicationComposeConventionPlugin"
        }

        register("androidLibraryCompose") {
            id = "artopos.android.library.compose"
            implementationClass = "ru.createsmart.artopos.AndroidLibraryComposeConventionPlugin"
        }

        register("detekt") {
            id = "artopos.convention.detekt"
            implementationClass = "ru.createsmart.artopos.DetektConventionPlugin"
        }
    }
}
