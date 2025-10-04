plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
}

ktlint {
    android.set(true)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
