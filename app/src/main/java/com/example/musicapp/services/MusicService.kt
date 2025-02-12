package com.example.musicapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.musicapp.R
import com.example.musicapp.di.MusicServiceEntryPoint
import com.example.musicapp.models.MyMusic
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : Service() {

    private lateinit var mediaPlayer : MediaPlayer

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer();
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try{
            val song = intent?.getParcelableExtra<MyMusic>("MUSIC")
            val action = intent?.getStringExtra("SONG_ACTION")
            val seekTo = intent?.getLongExtra("SEEK",-1L)
            if (song == null) {
                stopSelf()
                return START_NOT_STICKY
            }
            if(action == "pause"){
                if(::mediaPlayer.isInitialized && mediaPlayer.isPlaying){
                    mediaPlayer.pause()
                    startForeground(NOTIFICATION_ID, createNotification(song,false))
                }
                return START_STICKY;
            }

            if (seekTo != null) {
                CoroutineScope(Dispatchers.IO).launch{
                    playMusic(song,seekTo)
                }
            }
            return START_STICKY
        }catch(e:Exception){
            stopSelf()
            return START_NOT_STICKY
        }
    }

    private fun createNotification(song: MyMusic,isPlaying:Boolean): Notification {
        val channelId = "MUSIC_SERVICE_CHANNEL"

        val bitmap = runBlocking {
            getAlbumArt(this@MusicService,song.albumArtUri)
        }

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
           if (bitmap != null) {
               setImageViewBitmap(R.id.song_image, bitmap)
           }
            setImageViewResource(R.id.notification_play_pause,if(!isPlaying) R.drawable.play else R.drawable.pause)
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
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setCustomContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setDeleteIntent(dismissIntent)
            .build()
    }

    private fun getAlbumArt(context: Context, albumId: Long): Bitmap? {
        val albumArtUri = Uri.parse("content://media/external/audio/albumart/$albumId")
        try {
            context.contentResolver.openInputStream(albumArtUri)?.use { stream ->
                return BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }



    private fun playMusic(song: MyMusic,seekTo:Long) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(applicationContext, Uri.parse(song.uri))
            mediaPlayer.prepare()
            mediaPlayer.seekTo(seekTo.toInt())
            mediaPlayer.start()
            startForeground(NOTIFICATION_ID, createNotification(song,true))
        } catch (e: IOException) {
            stopSelf()
        } catch (e: IllegalStateException) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        if(::mediaPlayer.isInitialized){
            mediaPlayer.release()
        }
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1
    }
}
