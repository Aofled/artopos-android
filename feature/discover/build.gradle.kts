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
    implementation(projects.core.artworkCard)
    implementation(projects.core.navigation)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.paging.compose)

    implementation(libs.kotlinx.collections.immutable)

    testImplementation(libs.bundles.test.unit.full)
}
