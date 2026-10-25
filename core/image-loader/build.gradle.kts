plugins {
    id("artopos.android.library")
    id("artopos.di.hilt")
}

dependencies {
    implementation(libs.okhttp.logging)
    implementation(libs.coil)

    testImplementation(libs.bundles.test.unit.common)
}
