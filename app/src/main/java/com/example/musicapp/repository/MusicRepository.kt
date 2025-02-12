package com.example.musicapp.repository

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.musicapp.models.MyMusic
import com.example.musicapp.services.MusicService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaPlayer: MediaPlayer
) {
    private val _myMusic = MutableStateFlow<List<MyMusic>>(emptyList())
    val myMusic: StateFlow<List<MyMusic>> = _myMusic

    private val _currentMusic = MutableStateFlow<MyMusic?>(null)
    val currentMusic : StateFlow<MyMusic?> = _currentMusic

    private val _currentIndex = MutableStateFlow<Int?>(null)
    val currentIndex : StateFlow<Int?> = _currentIndex

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying

    private val _totalDuration = MutableStateFlow<Long>(0L)
    val totalDuration : StateFlow<Long> = _totalDuration

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _currentTime = MutableStateFlow<Long>(0L)
    val currentTime : StateFlow<Long> = _currentTime

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.ALBUM_ID
    )

    init {
        coroutineScope.launch {
            getMyMusics()
        }
    }

    private suspend fun getMyMusics() {
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val musics = mutableListOf<MyMusic>()
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val filePath = it.getString(dataColumn)
                val title = it.getString(titleColumn)
                val artist = it.getString(artistColumn)
                val duration = it.getLong(durationColumn)
                val albumId = it.getLong(albumIdColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    it.getLong(idColumn)
                )

                musics.add(MyMusic(id, filePath, title, artist, duration, albumId, uri.toString()))
            }
            _myMusic.value = musics;
        }
    }

    suspend fun play(index:Int){
        if (index < 0 || index >= _myMusic.value.size) return
        try {
            _currentIndex.value = index
            _currentMusic.value = _myMusic.value[index]
            _totalDuration.value = _myMusic.value[index].duration
            _isPlaying.value = true
            startMusicService(_myMusic.value[index],0L)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun toggleMediaPlayer(){
        try{
            if(_isPlaying.value){
                _isPlaying.value = false
                _currentMusic.value?.let { stopMusicService(it) }
            }else{
                _isPlaying.value = true
                _currentMusic.value?.let { startMusicService(it,currentTime.value) }
            }
        }catch (e:Exception){
            Log.d("New Exception","${e}")
        }
    }

    suspend fun updateTime(time:Long,type:String){
        if(type == "reset"){
            _currentTime.value = time
            return
        }
        _currentTime.value += time
    }


    suspend fun playNextTrack(){
        if(_currentIndex.value != null && _currentIndex.value != myMusic.value.lastIndex){
            val value = _currentIndex.value!! + 1;
            play(value)
        }else{
            toggleMediaPlayer()
        }
    }

    suspend fun playPreviousTrack(){
        if(_currentIndex.value != null && _currentIndex.value != 0){
            val value = _currentIndex.value!! - 1
            play(value)
        }
    }

    private fun stopMusicService(music: MyMusic) {
        val intent = Intent(context, MusicService::class.java).apply {
            putExtra("MUSIC", music)
            putExtra("SONG_ACTION", "pause")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    suspend fun dismissService(){
        _isPlaying.value = false
        val intent = Intent(context, MusicService::class.java)
        context.stopService(intent)
    }

    fun startMusicService(music: MyMusic,seek:Long) {
        val intent = Intent(context, MusicService::class.java).apply {
            putExtra("MUSIC", music)
            putExtra("SEEK",seek)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}