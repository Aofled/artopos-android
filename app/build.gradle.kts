plugins {
    id("artopos.android.application")
    id("artopos.android.application.compose")
    id("artopos.di.hilt")
}

android {
    namespace = "ru.createsmart.artopos"

    defaultConfig {
        applicationId = "ru.createsmart.artopos"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.designsystem)
    implementation(projects.core.domain)
    implementation(projects.core.imageLoader)
    implementation(projects.core.model)
    implementation(projects.core.navigation)
    implementation(projects.core.network)
    implementation(projects.core.translation)
    implementation(projects.core.uiComponents)
    implementation(projects.feature.details)
    implementation(projects.feature.discover)
    implementation(projects.feature.favorites)
    implementation(projects.feature.settings)

    implementation(libs.okhttp3)
    implementation(libs.coil.base)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.core.splashscreen)
}
