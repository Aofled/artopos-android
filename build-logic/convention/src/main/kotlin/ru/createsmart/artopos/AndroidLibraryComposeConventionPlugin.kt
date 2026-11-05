package ru.createsmart.artopos

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import ru.createsmart.artopos.extensions.configureAndroidCompose

/**
 * Adds Compose support to Library modules (Android Lib + Compose).
 * Automatically applies the base Android Library convention (SDK, Java 21, namespaces, Detekt).
 */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("artopos.android.library")

            val extension = extensions.getByType<LibraryExtension>()
            configureAndroidCompose(extension)
        }
    }
}
