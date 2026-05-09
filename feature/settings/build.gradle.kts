plugins {
    id("artopos.android.library")
    id("artopos.android.library.compose")
    id("artopos.di.hilt")
}

dependencies {
    implementation(projects.core.uiComponents)
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.domain)

    implementation(libs.androidx.hilt.navigation.compose)

    testImplementation(libs.bundles.test.unit.full)
}
