package ru.createsmart.artopos

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/**
 * Plugin for Pure Kotlin modules (Domain, pure logic).
 * Doesn't depend on the Android framework.
 */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("java-library")
                apply("org.jetbrains.kotlin.jvm")
                apply("artopos.convention.detekt")
            }

            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = org.gradle.api.JavaVersion.VERSION_21
                targetCompatibility = org.gradle.api.JavaVersion.VERSION_21
            }

            extensions.configure<KotlinProjectExtension> {
                explicitApi = ExplicitApiMode.Strict
            }

            tasks.withType(KotlinJvmCompile::class.java).configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
    }
}
