package com.example.musicapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.musicapp.R
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()  // Initialize MediaPlayer properly
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val songUri = intent?.getStringExtra("SONG_URI")
        val action = intent?.getStringExtra("SONG_ACTION")
        if(action == "pause"){
            mediaPlayer?.pause()
            return START_STICKY;
        }
        if (songUri.isNullOrEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        playMusic(songUri)

        return START_STICKY
    }

    private fun createNotification(songTitle: String, artistName: String): Notification {
        val channelId = "MUSIC_SERVICE_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Music Player Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(channelId) == null) {
                manager.createNotificationChannel(channel)
            }
        }

       val notificationLayout = RemoteViews(packageName, R.layout.notification_layout).apply {
            setTextViewText(R.id.notification_song_title, songTitle)
            setTextViewText(R.id.notification_song_artist, artistName)

            val playPauseIntent = PendingIntent.getBroadcast(
                this@MusicService, 0,
                Intent(this@MusicService, NotificationReceiver::class.java).apply {
                    action = "PLAY_PAUSE"
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.notification_play_pause, playPauseIntent)
        }

        val dismissIntent = PendingIntent.getBroadcast(
            applicationContext, 0,
            Intent(applicationContext, NotificationReceiver::class.java).apply {
                action = "NOTIFICATION_DISMISS"
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("Notification Layout","${notificationLayout.layoutId}")

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setCustomContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setDeleteIntent(dismissIntent)
            .build()
    }



    private fun playMusic(songUri: String) {
        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(applicationContext, Uri.parse(songUri))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            startForeground(NOTIFICATION_ID, createNotification("Music","Music"))
        } catch (e: IOException) {
            stopSelf()
        } catch (e: IllegalStateException) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1
    }
}
