plugins {
    id("artopos.android.library")
    id("artopos.di.hilt")
}

android {
    namespace = "ru.createsmart.artopos.core.data"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(projects.core.domain)
}
