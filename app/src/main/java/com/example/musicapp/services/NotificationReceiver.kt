package com.example.musicapp.services
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.CallSuper
import com.example.musicapp.di.MusicServiceEntryPoint
import com.example.musicapp.repository.MusicRepository
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationReceiver:BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if(context == null) return

        val musicRepo = EntryPointAccessors.fromApplication(
            context.applicationContext,
            MusicServiceEntryPoint::class.java
        ).getMusicRepository()

        when (intent.action) {
            "PLAY_PAUSE" -> {
                CoroutineScope(Dispatchers.IO).launch {
                    musicRepo.toggleMediaPlayer()
                }
            }
            "NOTIFICATION_DISMISS" -> {
                musicRepo.dismissService()
            }
        }
    }
}