import java.time.YearMonth
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sqldelight)
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0"
    alias(libs.plugins.tracer)
}

android {
    namespace = "io.github.pavelshel1.delta"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    defaultConfig {
        applicationId = "io.github.pavelshel1.delta"
        minSdk = 23
        targetSdk = 36
        versionCode = (project.findProperty("versionCode") as? String)?.toInt() ?: 1
        versionName = (project.findProperty("versionName") as? String) ?: "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "APP_DESCRIPTION",    "\"${System.getenv("APP_DESCRIPTION")     ?: "Калькулятор испытаний на герметичность · падение давления ΔP"}\"")
        buildConfigField("String", "AUTHOR",             "\"${System.getenv("AUTHOR")              ?: "PavelShe11"}\"")
        buildConfigField("String", "GITHUB_REPO_URL",    "\"${System.getenv("GITHUB_REPO_URL")     ?: "https://github.com/PavelShe11/Delta"}\"")
        buildConfigField("String", "GITHUB_PROFILE_URL", "\"${System.getenv("GITHUB_PROFILE_URL")  ?: "https://github.com/PavelShe11"}\"")
        buildConfigField("String", "LICENSE",            "\"${System.getenv("LICENSE")             ?: "MIT"}\"")
        buildConfigField(
            "String",
            "BUILD_LABEL",
            "\"${YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy.MM"))}\""
        )
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
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
        resValues = true
        buildConfig = true
    }
}

tracer {
    create("defaultConfig") {
        pluginToken = System.getenv("TRACER_PLUGIN_TOKEN") ?: ""
        appToken = System.getenv("TRACER_APP_TOKEN") ?: ""

        uploadMapping = true
        uploadNativeSymbols = true
        uploadRetryCount = 2
    }

    create("debug") {
        isDisabled = true
    }
}

dependencies {
    implementation(platform(libs.tracer.platform))
    // Сбор и анализ крешей и ANR
    implementation(libs.tracer.crash.report)
    // Сбор и анализ нативных крешей
    implementation(libs.tracer.crash.report.native)
    // Сбор и анализ хипдапмов при OOM
    implementation(libs.tracer.heap.dumps)
    // Анализ потребления дискового места на устройстве
    implementation(libs.tracer.disk.usage)
    // Семплирующий профайлер
    implementation(libs.tracer.profiler.sampling)
    // Систрейс
    implementation(libs.tracer.profiler.systrace)

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

