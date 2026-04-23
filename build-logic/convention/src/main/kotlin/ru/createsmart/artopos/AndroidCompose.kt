package ru.createsmart.artopos

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

/**
 * Configures Jetpack Compose (Compiler, features, dependencies)
 */
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension,
) {
    // Apply the new Compose Compiler plugin (Kotlin 2.0+)
    apply(plugin = "org.jetbrains.kotlin.plugin.compose")

    commonExtension.apply {
        buildFeatures.apply {
            compose = true
        }
    }

    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()

        // BOM manages versions. No need to specify versions for other Compose libs
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))

        // Add main Compose libraries (UI, Material, etc.)
        add("implementation", libs.findBundle("compose").get())
        // Add debug tools (Inspector, Previews). Only for Debug builds!
        add("debugImplementation", libs.findBundle("compose-debug").get())

        // Safer state collection: collectAsStateWithLifecycle()
        // Stops updating UI when app is in background (saves battery).
        add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
    }
}
