package com.example.musicapp.viewmodel

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.musicapp.models.MyMusic
import com.example.musicapp.repository.MusicRepository
import com.example.musicapp.services.MusicService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MyMusicVm @Inject constructor(
    @ApplicationContext private val context : Context,
    private val mediaPlayer: MediaPlayer,
    private val musicRepo: MusicRepository
):ViewModel() {

    val myMusic = musicRepo.myMusic
    val currentMusic = musicRepo.currentMusic
    val currentIndex = musicRepo.currentIndex
    val isPlaying = musicRepo.isPlaying
    val totalDuration  = musicRepo.totalDuration
    val currentTime = musicRepo.currentTime

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
        viewModelScope.launch {
            musicRepo.play(index)
        }
    }

    fun updateTime(time:Long,type:String){
        viewModelScope.launch {
            musicRepo.updateTime(time,type)
        }
    }

    fun playNextTrack(){
        viewModelScope.launch {
            musicRepo.playNextTrack()
        }
    }

    fun playPreviousTrack(){
        viewModelScope.launch {
            musicRepo.playPreviousTrack()
        }
    }


    fun toggleMediaPlayer(){
        viewModelScope.launch {
            musicRepo.toggleMediaPlayer()
        }
    }

    @SuppressLint("DefaultLocale")
    fun convertMillisToMinutesSeconds(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}