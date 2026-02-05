package ru.createsmart.artopos

import com.android.build.api.dsl.TestExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Configures Benchmark modules (Macrobenchmark) to measure performance
 */
class AndroidBenchmarkConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                // Special plugin for Benchmark/Test modules (not App, not Library)
                apply("com.android.test")
                apply("org.jetbrains.kotlin.android")
                apply("artopos.convention.detekt")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.configure<TestExtension> {
                configureKotlinAndroid(this)

                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    targetSdk = libs.findVersion("android-sdk-target").get().toString().toInt()
                }

                buildTypes {
                    create("benchmark") {
                        isDebuggable = true
                        signingConfig = getByName("debug").signingConfig
                        matchingFallbacks += listOf("release")
                    }
                }

                experimentalProperties["android.experimental.self-instrumenting"] = true
            }

            dependencies {
                add("implementation", libs.findBundle("benchmark").get())
            }
        }
    }
}
