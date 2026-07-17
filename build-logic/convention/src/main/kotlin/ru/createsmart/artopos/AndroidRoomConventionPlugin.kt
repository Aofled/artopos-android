package ru.createsmart.artopos

import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Configures Room Database (KSP, Schema export, Dependencies)
 */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("androidx.room")
            pluginManager.apply("com.google.devtools.ksp")

            extensions.configure<RoomExtension> {
                // Critical for Auto-Migrations.
                // Generates JSON schemas. COMMIT these files to Git!
                val schemaDir = layout.projectDirectory.dir("schemas").asFile.absolutePath
                schemaDirectory(schemaDir)
            }

            dependencies {
                // Auto-connect Room libraries
                add("implementation", libs.findBundle("room").get())
                add("ksp", libs.findLibrary("room-compiler").get())
            }
        }
    }
}
