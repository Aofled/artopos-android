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
}
