plugins {
    id("artopos.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.createsmart.artopos.core.navigation"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
