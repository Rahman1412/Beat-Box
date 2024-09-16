package com.example.musicapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicapp.R
import com.example.musicapp.models.MyMusic
import com.example.musicapp.viewmodel.MyMusicVm

@Composable
fun Musics(index:Int,music:MyMusic,vm:MyMusicVm,reset : () -> Unit){
    Row(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable {
                vm.play(index)
                reset()
            }
    ){
        Image(
            bitmap = vm.getAlbumArt(music.albumArtUri)?.asImageBitmap() ?: ImageBitmap.imageResource(id = R.drawable.music_icon),
            contentDescription = "Music Art",
            modifier = Modifier.size(65.dp)
        )
        Column(
            modifier = Modifier.padding(start = 5.dp)
        ) {
            MusicTitle(music.title)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(text = music.artist, color = MaterialTheme.colorScheme.onSurface)
                Text(text = " • ", fontSize = 16.sp,color = MaterialTheme.colorScheme.onSurface)
                Text(text = vm.convertMillisToMinutesSeconds(music.duration),color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}