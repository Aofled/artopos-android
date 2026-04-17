import java.util.Properties

plugins {
    id("artopos.android.library")
    id("artopos.di.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.createsmart.artopos.core.network"

    defaultConfig {
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        val apiKey = properties.getProperty("HARVARD_API_KEY") ?: ""

        buildConfigField("String", "HARVARD_API_URL", "\"https://api.harvardartmuseums.org/\"")
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.bundles.network)

    testImplementation(libs.bundles.test.unit.full)
    testImplementation(libs.kotlinx.serialization.json)
}
