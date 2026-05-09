plugins {
    id("artopos.android.benchmark")
}

android {
    targetProjectPath = ":app"
}

androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark"
    }
}
