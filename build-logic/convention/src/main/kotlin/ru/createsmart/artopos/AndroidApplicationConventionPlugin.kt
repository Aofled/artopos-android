package ru.createsmart.artopos

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import ru.createsmart.artopos.extensions.configureKotlinAndroid
import ru.createsmart.artopos.extensions.findVersionInt
import ru.createsmart.artopos.extensions.libs

/**
 * Configures the main App module (builds APK). Don't use in libraries.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("artopos.convention.detekt")
            }

            extensions.configure<ApplicationExtension> {
                // Apply base settings (minSdk, Java version)
                configureKotlinAndroid(this)

                // Settings specific for App (not for Libraries)
                defaultConfig.targetSdk = libs.findVersionInt("android-sdk-target")

                val vCode = providers.gradleProperty("ARTOPOS_VERSION_CODE")
                    .map { it.toIntOrNull() ?: 1 }
                    .getOrElse(1)
                defaultConfig.versionCode = vCode

                val vName = providers.gradleProperty("ARTOPOS_VERSION_NAME")
                    .getOrElse("1.0.0")
                defaultConfig.versionName = vName

                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
        }
    }
}
