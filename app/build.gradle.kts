plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.customerangkot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.customerangkot"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_URL", "\"http://192.168.101.23:8000/api/\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    // Glide Library
    implementation(libs.glide)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.lifecycle.runtime.ktx) // untuk lifecycleScope

    //Prefences
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v290)
    implementation(libs.androidx.lifecycle.livedata.ktx.v290)
    implementation(libs.androidx.activity.ktx)

    // Coroutines
    implementation (libs.kotlinx.coroutines.core)

    //Shimmer
    implementation(libs.shimmer)

    // Find My Location
    implementation(libs.play.services.maps.v1820)
    implementation(libs.play.services.location)

    // Pusher
    implementation(libs.pusher.java.client)

    // Polyline
    implementation ("com.google.maps.android:android-maps-utils:3.0.0")
}