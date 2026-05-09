plugins {
    id("artopos.android.library")
    id("artopos.di.hilt")
}

dependencies {
    implementation(projects.core.network)

    implementation(libs.okhttp.logging)
    implementation(libs.coil.base)

    testImplementation(libs.bundles.test.unit.full)
}
