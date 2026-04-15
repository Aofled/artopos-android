package ru.createsmart.artopos

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Static analysis & Formatting (Style guide)
 *
 * Simple check (For CI and locally): ./gradlew detektAll
 * (It will fail if there are formatting errors. The code will not change.)
 * Auto-correct (Locally before commit): ./gradlew detektAll
 */
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            val isAutoCorrectEnabled = providers.gradleProperty("detekt.autocorrect").orNull == "true"

            extensions.configure<DetektExtension>("detekt") {
                config.setFrom(files("$rootDir/config/detekt/detekt.yml"))

                source.setFrom(files("src"))

                // Combine default rules + our config
                buildUponDefaultConfig = true

                // Speed up analysis. Warning: High RAM usage!
                // Disable if Gradle crashes with OOM (OutOfMemory).
                parallel = true

                // Auto-fix simple errors (formatting)
                autoCorrect = isAutoCorrectEnabled
            }

            dependencies {
                // Add formatting rules (KtLint wrapper)
                add("detektPlugins", libs.findLibrary("detekt-formatting").get())
            }
        }
    }
}
