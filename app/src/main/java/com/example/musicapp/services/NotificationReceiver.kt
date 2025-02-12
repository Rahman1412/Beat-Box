package com.example.musicapp.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.musicapp.di.MusicServiceEntryPoint
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationReceiver:BroadcastReceiver() {
    private val coroutineScope = CoroutineScope(SupervisorJob()+Dispatchers.IO)
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null) return
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            MusicServiceEntryPoint::class.java
        )
        val musicRepository = entryPoint.getMusicRepository()
        when (intent?.action) {
            "PLAY_PAUSE" -> {
                coroutineScope.launch {
                    musicRepository.toggleMediaPlayer()
                }
            }
            "NOTIFICATION_DISMISS" -> {
                musicRepository.dismissService()
            }
        }
    }
}