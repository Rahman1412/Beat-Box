package com.example.musicapp.screens

import android.annotation.SuppressLint
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.musicapp.R
import com.example.musicapp.viewmodel.MyMusicVm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("Recycle")
@Composable
fun MyMusics(){
    val vm : MyMusicVm = viewModel()
    val myMusic = vm.myMusic.collectAsState().value

    val currentMusic = vm.currentMusic.value
    val isPlaying = vm.isPlaying.value
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var isBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    val totalDuration = vm.totalDuration.longValue
    var currentTime by rememberSaveable { mutableLongStateOf(0L) }
    val currentIndex = vm.currentIndex.value

    val reset : () -> Unit = {
        currentTime = 0L
    }

    LaunchedEffect(isPlaying) {
        withContext(Dispatchers.IO){
            if (isPlaying) {
                while (currentTime < totalDuration) {
                    delay(1000L)
                    currentTime += 1000L
                    if(currentTime >= totalDuration){
                        vm.playNextTrack()
                        reset()
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ){
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ){
            myMusic.forEachIndexed  { index,item->
                Musics(index,item,vm,reset)
            }
        }

        if(currentMusic != null){
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .align(Alignment.BottomEnd)
                    .clickable {
                        isBottomSheet = true
                    },
                elevation =  CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(0.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ){
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f)
                        ) {
                            Image(
                                bitmap = vm.getAlbumArt(currentMusic.albumArtUri)?.asImageBitmap()
                                    ?: ImageBitmap.imageResource(id = R.drawable.music_icon),
                                contentDescription = "Music Art",
                                modifier = Modifier.size(65.dp)
                            )
                            Column(
                                modifier = Modifier.padding(start = 5.dp)
                            ) {
                                MusicTitle(currentMusic.title)
                                Text(
                                    text = currentMusic.artist,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                vm.toggleMediaPlayer()
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = if (isPlaying) R.drawable.pause else R.drawable.play),
                                contentDescription = "Action",
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(5.dp)
                            )
                        }
                    }
                    MusicPlayerProgressBar(currentTime,totalDuration)
                }

            }
        }
    }

    if(isBottomSheet && currentMusic != null && currentIndex != null){
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    isBottomSheet = false
                    sheetState.hide()
                }
            },
            sheetState = sheetState,
            modifier = Modifier.fillMaxHeight(),
        ) {
            CurrentMusic(currentMusic,myMusic,vm,isPlaying,currentTime,totalDuration,reset,currentIndex)
        }
    }
}