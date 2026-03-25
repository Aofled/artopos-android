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
}
