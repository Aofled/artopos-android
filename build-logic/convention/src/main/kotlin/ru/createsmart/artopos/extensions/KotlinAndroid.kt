package ru.createsmart.artopos.extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.TestExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/**
 * Setup base Android settings for App and Library modules
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    commonExtension.apply {
        val moduleName = project.path.split(":")
            .drop(1)
            .joinToString(".")
            .replace("-", "")

        namespace = if (moduleName.isNotEmpty() && moduleName != "app") {
            "ru.createsmart.artopos.$moduleName"
        } else {
            "ru.createsmart.artopos"
        }

        compileSdk = libs.findVersionInt("android-sdk-compile")

        // Gradle Kotlin DSL quirks: requires casting to specific extensions
        // to access defaultConfig and compileOptions blocks in some AGP versions.
        when (this) {
            is ApplicationExtension -> {
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }
                defaultConfig {
                    minSdk = libs.findVersionInt("android-sdk-min")
                }
            }

            is LibraryExtension -> {
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }
                defaultConfig {
                    minSdk = libs.findVersionInt("android-sdk-min")
                }
            }

            is TestExtension -> {
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }
            }
        }
    }

    tasks.withType(KotlinJvmCompile::class.java).configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}
