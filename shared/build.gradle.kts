plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
}

val hostOs = System.getProperty("os.name")?.lowercase() ?: ""
val enableIosTargets = hostOs.contains("mac") || hostOs.contains("darwin")

kotlin {
    androidTarget()

    if (enableIosTargets) {
        listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
            target.binaries.framework {
                baseName = "Shared"
                isStatic = false
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.coroutines.core)
                implementation(libs.serialization.json)
                implementation(libs.koin.core)
                implementation(libs.napier)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.kotlinx.datetime)
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.noarg)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.coroutines.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.sqldelight.android)
            }
        }

        if (enableIosTargets) {
            val iosMain by creating {
                dependsOn(commonMain)
                dependencies {
                    implementation(libs.ktor.client.darwin)
                    implementation(libs.sqldelight.native)
                }
            }
            val iosX64Main by getting { dependsOn(iosMain) }
            val iosArm64Main by getting { dependsOn(iosMain) }
            val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
        }
    }
}

android {
    namespace = "com.calometer.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

sqldelight {
    databases {
        create("CalometerDatabase") {
            packageName.set("com.calometer.shared.database")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            migrationOutputDirectory.set(file("src/commonMain/sqldelight/migrations"))
        }
    }
}

// Helper task to sync framework for Xcode consumption
if (enableIosTargets) {
    tasks.register("syncFramework") {
        dependsOn("linkDebugFrameworkIosSimulatorArm64")
    }
} else {
    tasks.register("syncFramework") {
        doLast {
            logger.lifecycle("iOS framework sync is skipped because iOS targets are disabled on this host.")
        }
    }
}
