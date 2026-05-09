plugins {
    id("artopos.android.library")
    id("artopos.android.library.compose")
}

dependencies {
    api(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.common)
    // Architecture: Standard Java annotations (@Inject).
    // Allows Dependency Injection without depending on the heavy Hilt library.
    implementation(libs.javax.inject)
    implementation(libs.coil.base)
}
