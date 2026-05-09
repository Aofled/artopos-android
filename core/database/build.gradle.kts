plugins {
    id("artopos.android.library")
    id("artopos.di.hilt")
    id("artopos.convention.room")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
