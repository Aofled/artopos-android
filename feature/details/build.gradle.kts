plugins {
    id("artopos.android.library")
    id("artopos.android.library.compose")
    id("artopos.di.hilt")
}

android {
    namespace = "ru.createsmart.artopos.feature.details"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(projects.core.navigation)

    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.zoomable)

    testImplementation(libs.bundles.test.unit.minimal)
}
