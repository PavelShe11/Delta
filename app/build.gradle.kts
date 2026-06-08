plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sqldelight)
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0"
}

android {
    namespace = "io.github.pavelshel1.delta"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "io.github.pavelshel1.delta"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.essenty.lifecycle.coroutines)
    implementation(libs.essenty.back.handler)
    implementation(libs.decompose)
    implementation(libs.decompose.extensions.compose)
    implementation(libs.mvikotlin)
    implementation(libs.mvikotlin.main)
    implementation(libs.mvikotlin.coroutines)
    implementation(libs.mvikotlin.logging)
    implementation(libs.latex.base)
    implementation(libs.latex.parser)
    implementation(libs.latex.renderer)
    implementation(libs.haze)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation(libs.sqldelight.android.driver)
    implementation(libs.sqldelight.coroutines)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation("app.cash.sqldelight:sqlite-driver:2.3.2")
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

sqldelight {
    databases {
        create("DeltaDatabase") {
            packageName.set("io.github.pavelshel1.delta.db")
        }
    }
}