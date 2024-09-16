package com.example.musicapp.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.models.MyMusic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MyMusicVm(application: Application):AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext
    private val mediaPlayer = MediaPlayer()


    private val _myMusic = MutableStateFlow<List<MyMusic>>(emptyList())
    val myMusic : StateFlow<List<MyMusic>> = _myMusic

    private val _currentMusic = mutableStateOf<MyMusic?>(null)
    val currentMusic = _currentMusic

    private val _currentIndex = mutableStateOf<Int?>(null)
    val currentIndex = _currentIndex

    private val _isPlaying = mutableStateOf(false)
    val isPlaying = _isPlaying

    private val _totalDuration = mutableLongStateOf(0L)
    val totalDuration  = _totalDuration

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.ALBUM_ID
    )

    init {
        getMyMusics()
    }


    private fun getMyMusics(){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
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
                        musics.add(MyMusic(id,filePath,title,artist,duration,albumId))
                    }
                    _myMusic.value = musics;
                }
            }
        }
    }


    @SuppressLint("DefaultLocale")
    fun convertMillisToMinutesSeconds(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getAlbumArt(albumId: Long): Bitmap? {
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


    fun play(index:Int){
        if (index < 0 || index >= _myMusic.value.size) return
        mediaPlayer.reset()
        try {
            _currentIndex.value = index
            _currentMusic.value = _myMusic.value[index]
            _totalDuration.longValue = _myMusic.value[index].duration
            _isPlaying.value = true
            mediaPlayer.setDataSource(_myMusic.value[index].filePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleMediaPlayer(){
        if(_isPlaying.value){
            _isPlaying.value = false
            mediaPlayer.pause()
        }else{
            _isPlaying.value = true
            mediaPlayer.start()
        }
    }


    fun playNextTrack(){
        if(_currentIndex.value != null && _currentIndex.value != _myMusic.value.lastIndex){
            val value = _currentIndex.value!! + 1;
            play(value)
        }else{
            toggleMediaPlayer()
        }
    }

    fun playPreviousTrack(){
        if(_currentIndex.value != null && _currentIndex.value != 0){
            val value = _currentIndex.value!! - 1
            play(value)
        }
    }


}