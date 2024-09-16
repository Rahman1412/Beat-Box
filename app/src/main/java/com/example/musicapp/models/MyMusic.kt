package com.example.musicapp.models

data class MyMusic(
    val id: Long,
    val filePath: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val albumArtUri: Long
)
