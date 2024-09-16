package com.example.musicapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.musicapp.R
import com.example.musicapp.models.MyMusic
import com.example.musicapp.viewmodel.MyMusicVm

@Composable
fun CurrentMusic(
        currentMusic:MyMusic,
        myMusic: List<MyMusic>,
        vm:MyMusicVm,
        isPlaying:Boolean,
        currentTime:Long,
        totalDuration:Long,
        reset: () -> Unit,
        currentIndex:Int
    ){
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Column(
            modifier = Modifier.fillMaxSize()
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Image(
                    bitmap = vm.getAlbumArt(currentMusic.albumArtUri)?.asImageBitmap() ?: ImageBitmap.imageResource(id = R.drawable.music_icon),
                    contentDescription = "Music Art",
                    modifier = Modifier.size(320.dp)
                )
                Text(
                    text = currentMusic.title,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(300.dp)
                        .padding(10.dp)
                )
                Row(
                    modifier = Modifier.width(320.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    IconButton(
                        onClick = {
                            vm.playPreviousTrack()
                            reset()
                        },
                        enabled = currentIndex != 0
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.previous),
                            contentDescription = "Previous",
                            modifier = Modifier.size(60.dp).padding(10.dp)
                        )
                    }

                    IconButton(onClick = {
                        vm.toggleMediaPlayer()
                    }) {
                        Icon(
                            painter = painterResource(id = if(isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = "Action",
                            modifier = Modifier.size(60.dp).padding(10.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            vm.playNextTrack()
                            reset()
                        },
                        enabled = currentIndex != myMusic.lastIndex
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.next),
                            contentDescription = "Previous",
                            modifier = Modifier.size(60.dp).padding(10.dp)
                        )
                    }
                }

                MusicPlayerProgressBar(currentTime,totalDuration)

            }
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 50.dp)
                    .horizontalScroll(rememberScrollState())
            ){
                myMusic.forEachIndexed{ index,item->
                    Card(
                        modifier = Modifier
                            .padding(10.dp)
                            .width(160.dp)
                            .height(280.dp)
                            .clickable {
                                vm.play(index)
                                reset()
                            }
                    ){
                        Image(
                            bitmap = vm.getAlbumArt(item.albumArtUri)?.asImageBitmap() ?: ImageBitmap.imageResource(id = R.drawable.music_icon),
                            contentDescription = "Music Art",
                            modifier = Modifier.size(160.dp)
                        )
                        Text(
                            text = item.title,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}