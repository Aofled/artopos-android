package ru.createsmart.artopos

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import ru.createsmart.artopos.extensions.libs

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

            // Disable auto-correct on CI.
            val isCi = providers.environmentVariable("CI").isPresent
            val isAutoCorrectProperty = providers.gradleProperty("detekt.autocorrect").getOrElse("false") == "true"
            val isAutoCorrectEnabled = !isCi && isAutoCorrectProperty

            extensions.configure<DetektExtension>("detekt") {
                // Locate config safely for Isolated Projects.
                val configFile = rootProject.layout.projectDirectory.file("config/detekt/detekt.yml")

                config.setFrom(files(configFile))
                source.setFrom(files("src"))

                // Combine default rules + our config
                buildUponDefaultConfig = true

                // Speed up analysis. Warning: High RAM usage!
                // Disable if Gradle crashes with OOM (OutOfMemory).
                parallel = true

                // Auto-fix simple errors (formatting)
                autoCorrect = isAutoCorrectEnabled
            }

            tasks.withType<Detekt>().configureEach {
                autoCorrect = isAutoCorrectEnabled
            }

            tasks.register("detektAll") {
                group = "verification"
                description = "Runs detekt for this module"
                dependsOn(tasks.named("detekt"))
            }

            dependencies {
                // Add formatting rules (KtLint wrapper)
                add("detektPlugins", libs.findLibrary("detekt-formatting").get())
            }
        }
    }
}
