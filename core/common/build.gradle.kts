plugins {
    id("artopos.android.library")
    id("artopos.di.hilt")
}

android {
    namespace = "ru.createsmart.artopos.core.common"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.network)

    implementation(libs.okhttp3)
    implementation(libs.coil.base)
}
