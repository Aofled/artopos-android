// Top-level build file where you can add configuration options common to all sub-projects/modules.

/**
 * === DETEKT COMMANDS CHEAT SHEET ===
 *
 * 1. RUN ALL CHECKS (Project + Build Logic)
 *    Runs checks on all modules including build-logic.
 *    Command: ./gradlew detektAll
 *
 * 2. STANDARD CHECK (Main Project Only)
 *    Scans app and features, logs errors to console, generates HTML report.
 *    Fails build if errors found.
 *    Command: ./gradlew detekt
 *
 * 3. AUTO-CORRECT (Main Project Only)
 *    Fixes formatting (spaces, indents, imports) automatically.
 *    Command: ./gradlew detekt --auto-correct
 *
 * 4. CHECK SPECIFIC MODULE
 *    Runs checks only for the specified module (faster).
 *    Command: ./gradlew :feature:discover:detekt
 *
 * 5. CHECK BUILD LOGIC
 *    Scans the build-logic (convention plugins) directory.
 *    Command: ./gradlew -p build-logic detekt
 *
 * 6. AUTO-CORRECT BUILD LOGIC
 *    Fixes formatting inside build-logic.
 *    Command: ./gradlew -p build-logic detekt --auto-correct
 *
 * 7. GENERATE CONFIG
 *    Updates detekt.yml with default values.
 *    WARNING: Will remove all your custom comments in yaml file!
 *    Command: ./gradlew detektGenerateConfig
 */

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}

// To clean the application gradle module correctly (Build -> Clean Project or ./gradlew clean)
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

// Helper task: Run Detekt for ALL modules
tasks.register("detektAll") {
    // Check build-logic code too
    dependsOn(gradle.includedBuild("build-logic").task(":convention:detekt"))
    dependsOn(
        subprojects
            // Run only on leaf modules (skip empty containers)
            .filter { it.childProjects.isEmpty() }
            .map { it.tasks.named("detekt") }
    )
}
