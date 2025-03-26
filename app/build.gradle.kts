plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    id("androidx.navigation.safeargs")


    kotlin("kapt")
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
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.navigation.fragment)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation (libs.glide)
    implementation ("com.github.bumptech.glide:glide:4.12.0")  // Use the correct version

    annotationProcessor (libs.compiler)
    implementation(libs.hilt.android) // ✅ Hilt Core
    kapt(libs.hilt.compiler) // ✅ Hilt Compiler
    // Hilt testing dependencies
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
    implementation ("com.google.android.material:material:1.9.0")
    implementation(libs.converter.scalars)
    implementation ("androidx.appcompat:appcompat:1.2.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("androidx.core:core-ktx:1.5.0")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.compiler)
    implementation ("androidx.core:core-ktx:1.9.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")  // Replace with the latest version


}
