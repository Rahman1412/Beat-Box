package com.example.musicapp.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.musicapp.repository.MusicRepository
import com.example.musicapp.services.MusicService

class AppLifecycleObserver(
    private val context:Context,
    private val musicRepo: MusicRepository
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        musicRepo.dismissService()
    }

}