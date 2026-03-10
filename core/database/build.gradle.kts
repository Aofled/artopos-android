plugins {
    id("artopos.android.library")
    id("artopos.di.hilt")
    id("artopos.convention.room")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.createsmart.artopos.core.database"
}

dependencies {
    implementation(projects.core.model)
    implementation(libs.kotlinx.serialization.json)
}
