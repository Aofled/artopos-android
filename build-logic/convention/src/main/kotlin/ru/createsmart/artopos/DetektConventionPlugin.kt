package ru.createsmart.artopos

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import ru.createsmart.artopos.extensions.libs

/**
 * Static analysis & Formatting (Style guide)
 *
 * Check code style: ./gradlew detektAll
 * Auto-fix code style: ./gradlew detektFormat
 */
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            val configFile = rootProject.layout.projectDirectory.file("config/detekt/detekt.yml")

            extensions.configure<DetektExtension>("detekt") {
                config.setFrom(files(configFile))
                source.setFrom(files("src"))

                // Combine default rules + our config
                buildUponDefaultConfig = true

                // Speed up analysis. Warning: High RAM usage!
                // Disable if Gradle crashes with OOM (OutOfMemory).
                parallel = true
                // Standard Detekt runs in read-only mode by default.
                autoCorrect = false
            }

            // Formatting task: fixes style issues in place.
            tasks.register<Detekt>("detektFormat") {
                group = "formatting"
                description = "Reformats code and fixes auto-correctable style issues for this module"
                setSource(files("src"))
                config.setFrom(files(configFile))
                buildUponDefaultConfig = true
                parallel = true
                autoCorrect = true

                // Disable reports for faster formatting.
                reports {
                    xml.required.set(false)
                    html.required.set(false)
                    txt.required.set(false)
                    sarif.required.set(false)
                }
            }

            // Verification task: runs code audit without modifying files.
            tasks.register("detektAll") {
                group = "verification"
                description = "Runs read-only Detekt check for this module"
                dependsOn(tasks.named("detekt"))
            }

            tasks.withType<Detekt>().configureEach {
                if (name == "detektFormat") {
                    autoCorrect = true
                }
            }

            dependencies {
                // Add formatting rules (KtLint wrapper)
                add("detektPlugins", libs.findLibrary("detekt-formatting").get())
            }
        }
    }
}
