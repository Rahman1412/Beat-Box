package com.example.musicapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MyMusic(
    val id: Long,
    val filePath: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val albumArtUri: Long,
    val uri : String
) : Parcelable
