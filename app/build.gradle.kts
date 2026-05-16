plugins {
    id("artopos.android.application")
    id("artopos.android.application.compose")
    id("artopos.di.hilt")
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    defaultConfig {
        applicationId = "ru.createsmart.artopos"

        ndk {
            abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    signingConfigs {
        create("release") {
            val path = System.getenv("KEYSTORE_PATH") ?: "keystore.jks"
            storeFile = file(path)
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    @Suppress("UnstableApiUsage")
    bundle {
        language {
            enableSplit = false
        }
    }

    @Suppress("UnstableApiUsage")
    androidResources {
        localeFilters += listOf("en", "ru", "fr", "be", "ja", "zh", "de", "it", "es")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }

    packaging {
        resources {
            excludes += "/kotlin/**"
            excludes += "/META-INF/androidx.*.version"
            excludes += "/META-INF/kotlinx_*"
            excludes += "/META-INF/com.google.*.version"
            excludes += "/DebugProbesKt.bin"
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

    implementation(libs.androidx.profileinstaller)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
}
