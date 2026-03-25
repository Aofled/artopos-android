plugins {
    id("artopos.android.library")
    id("artopos.android.library.compose")
    id("artopos.di.hilt")
}

android {
    namespace = "ru.createsmart.artopos.feature.settings"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.datastore)
    implementation(projects.core.common)
    implementation(projects.core.domain)

    implementation(libs.androidx.hilt.navigation.compose)

    testImplementation(libs.bundles.test.unit.full)
}
