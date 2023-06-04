@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
    id("kotlin-kapt")
}

android {
    namespace = "com.m3u.data"
    compileSdk = 33
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    ksp {
        arg("room.schemaLocation", "${projectDir}/schemas")
        arg("ksp.incremental", "true")
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.appcompat.appcompat)

    implementation(libs.androidx.room.room.runtime)
    implementation(libs.androidx.room.room.ktx)
    ksp(libs.androidx.room.room.compiler)

    implementation(libs.com.google.dagger.hilt.android)
    kapt(libs.com.google.dagger.hilt.compiler)

    implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)

    implementation(libs.io.coil.kt.coil)

    implementation(libs.com.squareup.retrofit2.retrofit)

    implementation(libs.androidx.media3.media3.exoplayer)
    implementation(libs.androidx.media3.media3.exoplayer.dash)
    implementation(libs.androidx.media3.media3.exoplayer.hls)
    implementation(libs.androidx.media3.media3.exoplayer.rtsp)
    implementation(libs.androidx.media3.media3.session)
    implementation(libs.androidx.media3.media3.datasource.rtmp)
    implementation(libs.androidx.media3.media3.datasource.okhttp)
    implementation(libs.androidx.media3.media3.extractor)

    api("com.eclipsesource.j2v8:j2v8:6.2.1@aar")
}