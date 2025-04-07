package com.hi.recipeapp

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize ThreeTenABP for backporting java.time to lower API levels
        AndroidThreeTen.init(this)
    }
}
