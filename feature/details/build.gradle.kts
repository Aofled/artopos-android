plugins {
    id("artopos.android.library")
    id("artopos.android.library.compose")
    id("artopos.di.hilt")
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.uiComponents)
    implementation(projects.core.navigation)

    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.kotlinx.collections.immutable)

    implementation(libs.coil.compose)
    implementation(libs.zoomable)

    testImplementation(libs.bundles.test.unit.full)
}
