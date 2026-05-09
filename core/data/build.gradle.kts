plugins {
    id("artopos.android.library")
    id("artopos.di.hilt")
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.domain)
    implementation(projects.core.imageLoader)
    implementation(projects.core.model)
    implementation(projects.core.network)
    implementation(projects.core.translation)

    implementation(libs.paging.runtime)
    implementation(libs.room.ktx)
    implementation(libs.retrofit.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.bundles.test.unit.full)
}
