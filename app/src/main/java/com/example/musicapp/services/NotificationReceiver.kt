package com.example.musicapp.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "PLAY_PAUSE" -> {
                Toast.makeText(context, "Play/Pause Clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }
}