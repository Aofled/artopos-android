plugins {
    id("artopos.android.library")
    id("artopos.android.library.compose")
    id("artopos.di.hilt")
}

android {
    namespace = "ru.createsmart.artopos.feature.favorites"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.uiComponents)
    implementation(projects.core.artworkCard)
    implementation(projects.core.navigation)

    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.kotlinx.collections.immutable)

    testImplementation(libs.bundles.test.unit.full)
}
