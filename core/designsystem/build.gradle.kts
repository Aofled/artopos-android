plugins {
    id("artopos.android.library")
    id("artopos.android.library.compose")
}

dependencies {
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(projects.core.model)
}
