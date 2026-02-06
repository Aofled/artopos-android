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
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(projects.core.data)
    implementation(projects.core.ui)
    implementation(projects.feature.discover)
    implementation(projects.core.database)
    implementation(projects.core.network)
}
