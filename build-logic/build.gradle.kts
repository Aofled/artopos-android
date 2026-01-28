plugins {

}

repositories {
    google()
    mavenCentral()
}

// To clean the build-logic module correctly (Build -> Clean Project or ./gradlew clean)
tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
