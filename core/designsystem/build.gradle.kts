plugins {
    id("artopos.android.library")
    id("artopos.android.library.compose")
}

android {
    namespace = "ru.createsmart.artopos.core.designsystem"
}

dependencies {
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(projects.core.common)
    implementation(projects.core.model)
    // Architecture: Standard Java annotations (@Inject).
    // Allows Dependency Injection without depending on the heavy Hilt library.
    implementation(libs.javax.inject)
}
