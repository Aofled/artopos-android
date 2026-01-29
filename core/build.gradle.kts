plugins {

}

// To clean the core module correctly (Build -> Clean Project or ./gradlew clean)
tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
