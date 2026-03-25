plugins {
    id("artopos.android.library")
    id("artopos.android.library.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.createsmart.artopos.core.navigation"
}

dependencies {
    implementation(projects.core.designsystem)

    implementation(libs.kotlinx.serialization.json)
}
