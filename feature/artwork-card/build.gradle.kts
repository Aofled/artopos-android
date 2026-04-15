plugins {
    id("artopos.android.library")
    id("artopos.android.library.compose")
}

android {
    namespace = "ru.createsmart.artopos.feature.artworkcard"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.model)
    implementation(projects.core.uiComponents)

    implementation(libs.coil.compose)

    testImplementation(libs.bundles.test.unit.minimal)
}
