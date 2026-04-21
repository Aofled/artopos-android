plugins {
    id("artopos.android.library")
    id("artopos.android.library.compose")
}

android {
    namespace = "ru.createsmart.artopos.feature.artworkcard"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.model)
    implementation(projects.core.uiComponents)

    implementation(libs.coil.compose)
    // Architecture: Standard Java annotations (@Inject).
    // Allows Dependency Injection without depending on the heavy Hilt library.
    implementation(libs.javax.inject)

    testImplementation(libs.bundles.test.unit.minimal)
}
