// Top-level build file where you can add configuration options common to all sub-projects/modules.

/**
 * ==========================================
 *  ARTOPOS COMMANDS CHEAT SHEET
 * ==========================================
 *
 * --- STATIC ANALYSIS (DETEKT & LINT) ---
 * 1. RUN DETEKT (Code Style & Smells)
 *    Checks all modules including `build-logic`.
 *    Command: ./gradlew detektAll
 *    Note: Auto-correction is enabled locally via `gradle.properties`.
 *
 * 2. RUN ANDROID LINT (Resources, Manifest, Android API)
 *    Checks for Android-specific issues (e.g. Unused resources, accessibility).
 *    Command: ./gradlew lintDebug
 *
 * 3. RUN ALL CHECKS (CI Simulation)
 *    Run this before pushing to trigger the same checks as GitHub Actions.
 *    Command: ./gradlew detektAll lintDebug
 *
 * --- TESTING ---
 * 4. RUN ALL UNIT TESTS
 *    Runs tests across all modules.
 *    Command: ./gradlew testDebugUnitTest
 *
 * 5. FORCE RERUN TESTS (No Cache)
 *    Ignores Gradle cache and forces all tests to execute again.
 *    Command: ./gradlew testDebugUnitTest --rerun-tasks
 *
 * --- DETEKT MAINTENANCE ---
 * 6. GENERATE DETEKT CONFIG
 *    Updates detekt.yml with default values for new rules.
 *    WARNING: This will overwrite any custom comments in your detekt.yml file!
 *    Command: ./gradlew detektGenerateConfig
 *
 * --- R8 CONFIGURATION ---
 * 7. CREATES R8 REPORT
 *    Creates a report in three directions: shrinking, obfuscation, optimization
 *    Command: ./gradlew :app:analyzeReleaseR8Config
 * ==========================================
 */

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
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
