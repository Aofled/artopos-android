plugins {
    id("artopos.android.library")
    id("artopos.di.hilt")
}

dependencies {
    implementation(libs.google.mlkit.translate)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)

}
