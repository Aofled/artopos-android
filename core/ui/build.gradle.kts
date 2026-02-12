plugins {
    id("artopos.android.library")
    id("artopos.android.library.compose")
}

android {
    namespace = "ru.createsmart.artopos.core.ui"
}

dependencies {
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(projects.core.common)
}
