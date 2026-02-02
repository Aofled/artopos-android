plugins {

}

// To clean the feature module correctly (Build -> Clean Project or ./gradlew clean)
tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
