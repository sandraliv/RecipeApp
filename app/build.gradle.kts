plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // The KSP plugin
    alias(libs.plugins.kotlin.ksp)
    // Hilt plugin
    alias(libs.plugins.hilt.android)

    // Safe Args (if you need it)
    id("androidx.navigation.safeargs")
    id("kotlin-kapt")
}

android {
    namespace = "com.hi.recipeapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hi.recipeapp"
        minSdk = 24
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


    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

}

dependencies {
    //Testing

    // Local unit tests in src/test/ use standard JUnit
    testImplementation(libs.junit)

    // Android instrumented tests in src/androidTest/
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // If you’re using the instrumented Hilt test library as well:
    androidTestImplementation(libs.hilt.android.testing)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.activity)

    // Retrofit og OKHTTP
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.converter.scalars)
    implementation(libs.okhttp)

// ─────────────────────────────────────────────────────────────────────────────
    // Hilt (KSP)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // ─────────────────────────────────────────────────────────────────────────────
    // Room (KSP)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ─────────────────────────────────────────────────────────────────────────────
    // Glide
    // Glide does NOT yet have an official KSP-based processor.
    // So you can use annotationProcessor (Java APT) or kapt (Kotlin APT).
    // If it's a pure Java library, annotationProcessor is enough:
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    implementation ("com.jakewharton.threetenabp:threetenabp:1.3.1")


}
