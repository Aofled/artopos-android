plugins {
    id("artopos.android.library")
    id("artopos.android.library.compose")
    id("artopos.di.hilt")
}

android {
    namespace = "ru.createsmart.artopos.feature.discover"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.paging.compose)

    testImplementation(libs.bundles.test.unit.minimal)
}
