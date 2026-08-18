package ru.createsmart.artopos.extensions

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/**
 * Configures Jetpack Compose (Compiler, features, dependencies)
 * Compose Compiler Metrics & Reports
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

    // Metrics generation is enabled only when passing a flag, so as not to slow down the regular build.
    // Usage in the terminal: ./gradlew assembleDebug -PenableComposeCompilerReports=true --rerun-tasks
    val enableReports = providers.gradleProperty("enableComposeCompilerReports").orNull == "true"
    if (enableReports) {
        extensions.configure<ComposeCompilerGradlePluginExtension> {
            val metricsDir = layout.buildDirectory.dir("compose_metrics")
            metricsDestination.set(metricsDir)
            reportsDestination.set(metricsDir)
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
