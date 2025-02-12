package com.example.musicapp

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.musicapp.repository.MusicRepository
import com.example.musicapp.utils.AppLifecycleObserver
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HiltApp : Application() {
}