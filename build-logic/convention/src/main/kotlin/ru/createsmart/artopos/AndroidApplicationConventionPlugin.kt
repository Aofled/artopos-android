package ru.createsmart.artopos

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

/**
 * Configures the main App module (builds APK). Don't use in libraries.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.configure<ApplicationExtension> {
                // Apply base settings (minSdk, Java version)
                configureKotlinAndroid(this)

                // Settings specific for App (not for Libraries)
                defaultConfig.targetSdk =
                    libs.findVersion("android-sdk-target").get().toString().toInt()

                defaultConfig.versionCode =
                    libs.findVersion("version-code").get().toString().toInt()

                defaultConfig.versionName =
                    libs.findVersion("version-name").get().toString()

                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
        }
    }
}
