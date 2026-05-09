plugins {
    id("artopos.android.library")
    id("artopos.di.hilt")
}

dependencies {
    implementation(projects.core.model)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.bundles.test.unit.full)
}
