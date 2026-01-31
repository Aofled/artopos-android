plugins {
    id("artopos.jvm.library")
}

dependencies {
    implementation(projects.core.model)
    // Architecture: Standard Java annotations (@Inject).
    // Allows Dependency Injection without depending on the heavy Hilt library.
    implementation(libs.javax.inject)
    // Async support. Use 'core' version, not 'android'.
    // Domain layer must NOT know about Android (Main Thread / Context).
    implementation(libs.kotlinx.coroutines.core)
}
