package ru.createsmart.artopos

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import ru.createsmart.artopos.extensions.libs

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

            dependencies {
                add("implementation", libs.findLibrary("hilt.android").get())
                add("ksp", libs.findLibrary("hilt.compiler").get())
            }
        }
    }
}
