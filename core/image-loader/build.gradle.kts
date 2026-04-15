plugins {
    id("artopos.android.library")
    id("artopos.di.hilt")
}

android {
    namespace = "ru.createsmart.artopos.core.imageloader"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.network)

    implementation(libs.coil.base)
}
