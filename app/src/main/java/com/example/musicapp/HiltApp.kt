package com.example.musicapp

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HiltApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("Hilt OnCreate","Hilt Is Initializing")
    }
}