plugins {
    id("artopos.android.library")
    id("artopos.android.library.compose")
}

android {
    namespace = "ru.createsmart.artopos.core.uicomponents"
}

dependencies {
    api(projects.core.designsystem)
    implementation(projects.core.common)
    // Architecture: Standard Java annotations (@Inject).
    // Allows Dependency Injection without depending on the heavy Hilt library.
    implementation(libs.javax.inject)
}
