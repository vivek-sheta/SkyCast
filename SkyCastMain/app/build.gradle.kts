plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.skycast"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.skycast"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}
apply(plugin = "com.google.gms.google-services")
dependencies {
    implementation(libs.firebase.auth.v2230)
    implementation(libs.play.services.auth)
    implementation("com.google.gms:google-services:4.3.2")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.lifecycle.runtime)

    // Optional: Material Design, AppCompat, etc.
    implementation(libs.material.v1100)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout.v214)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.annotation)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}