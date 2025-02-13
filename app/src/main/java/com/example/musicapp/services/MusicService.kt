package com.example.musicapp.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.musicapp.R
import com.example.musicapp.di.MusicServiceEntryPoint
import com.example.musicapp.models.MyMusic
import com.example.musicapp.repository.MusicRepository
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

class MusicService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var notificationManager: NotificationManager
    private var currentSong: MyMusic? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val song = intent?.getParcelableExtra<MyMusic>("MUSIC")
            val action = intent?.getStringExtra("SONG_ACTION")
            val seekTo = intent?.getLongExtra("SEEK", -1L)

            if (song == null) {
                stopSelf()
                return START_NOT_STICKY
            }
            currentSong = song

            when (action) {
                "pause" -> pauseMusic()
                "play" -> {
                    if (seekTo != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            playMusic(song, seekTo)
                        }
                    }
                }
                else -> {
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
            return START_STICKY
        } catch (e: Exception) {
            stopSelf()
            return START_NOT_STICKY
        }
    }

    private fun pauseMusic() {
        Log.d("Pause Music","try to pause music")
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            updateNotification(false)
        }
    }

    private fun resumeMusic() {
        if (::mediaPlayer.isInitialized && !mediaPlayer.isPlaying) {
            mediaPlayer.start()
            updateNotification(true)
        }
    }

    private fun updateNotification(isPlaying: Boolean) {
        currentSong?.let {
            val notification = createNotification(it, isPlaying)
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotification(song: MyMusic, isPlaying: Boolean): Notification {
        val channelId = "MUSIC_SERVICE_CHANNEL"

        val bitmap = runBlocking {
            getAlbumArt(this@MusicService, song.albumArtUri)
        }

        val notificationLayout = RemoteViews(packageName, R.layout.notification_layout).apply {
            if (bitmap != null) {
                setImageViewBitmap(R.id.song_image, bitmap)
            }
            setImageViewResource(R.id.notification_play_pause, if (!isPlaying) R.drawable.play else R.drawable.pause)
            setTextViewText(R.id.notification_song_title, song.title)
            setTextViewText(R.id.notification_song_artist, song.artist)

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

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.music_icon)
            .setCustomContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(isPlaying)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setDeleteIntent(dismissIntent)
            .build()
    }

    private fun getAlbumArt(context: Context, albumId: Long): Bitmap? {
        val albumArtUri = Uri.parse("content://media/external/audio/albumart/$albumId")
        return try {
            context.contentResolver.openInputStream(albumArtUri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun playMusic(song: MyMusic, seekTo: Long) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(applicationContext, Uri.parse(song.uri))
            mediaPlayer.prepare()
            mediaPlayer.seekTo(seekTo.toInt())
            mediaPlayer.start()
            startForeground(NOTIFICATION_ID, createNotification(song, true))
        } catch (e: IOException) {
            stopSelf()
        } catch (e: IllegalStateException) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        Log.d("MusicService", "Service destroyed")
        try {
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            }
        } catch (e: IllegalStateException) {
            Log.e("MusicService", "Error while releasing MediaPlayer: ${e.message}")
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d("MusicService", "onTaskRemoved called. Stopping service.")
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            System.exit(0)
        } catch (e: Exception) {
            Log.e("MusicService", "Error stopping service: ${e.message}")
        }
        super.onTaskRemoved(rootIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MUSIC_SERVICE_CHANNEL",
                "Music Player Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1
    }
}
