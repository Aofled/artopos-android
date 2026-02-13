package ru.createsmart.artopos

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Configures Library modules (Features, Core). No APK here.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("artopos.convention.detekt")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)

                // Optimization: Disable unnecessary BuildConfig generation
                buildFeatures {
                    buildConfig = false
                }

                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
        }
    }
}
