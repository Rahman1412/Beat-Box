package com.example.musicapp.di

import android.media.MediaPlayer
import com.example.musicapp.repository.MusicRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MusicServiceEntryPoint {
    fun getMediaPlayer() : MediaPlayer

    fun getMusicRepository() : MusicRepository
}