plugins {
    id("artopos.android.benchmark")
}

android {
    namespace = "ru.createsmart.artopos.benchmark"
    targetProjectPath = ":app"
}

androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark"
    }
}
