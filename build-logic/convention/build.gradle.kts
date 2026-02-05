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

    implementation(libs.hilt.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)

    implementation(libs.room.gradlePlugin)
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

        // Pure Kotlin Library (No Android dependencies)
        register("jvmLibrary") {
            id = "artopos.jvm.library"
            implementationClass = "ru.createsmart.artopos.JvmLibraryConventionPlugin"
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

        register("hilt") {
            id = "artopos.di.hilt"
            implementationClass = "ru.createsmart.artopos.HiltConventionPlugin"
        }

        register("room") {
            id = "artopos.convention.room"
            implementationClass = "ru.createsmart.artopos.AndroidRoomConventionPlugin"
        }

        // Performance Testing (Startup, Frames)
        register("androidBenchmark") {
            id = "artopos.android.benchmark"
            implementationClass = "ru.createsmart.artopos.AndroidBenchmarkConventionPlugin"
        }
    }
}
