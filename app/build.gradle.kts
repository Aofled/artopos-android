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
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
