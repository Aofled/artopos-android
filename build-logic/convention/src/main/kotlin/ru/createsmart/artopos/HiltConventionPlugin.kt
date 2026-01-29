package ru.createsmart.artopos

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Configures Dependency Injection (Hilt + KSP)
 */
class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                // Use KSP (faster) instead of KAPT
                apply("com.google.devtools.ksp")
                apply("com.google.dagger.hilt.android")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                // Auto-add Hilt libraries. No need to add them manually in modules.
                "implementation"(libs.findLibrary("hilt.android").get())
                "ksp"(libs.findLibrary("hilt.compiler").get())
            }
        }
    }
}
