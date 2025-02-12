package com.example.musicapp.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.example.musicapp.services.MusicService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
): ViewModel(), LifecycleEventObserver{
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when(event){
            Lifecycle.Event.ON_CREATE -> Log.d("Created","Activity Created")
            Lifecycle.Event.ON_START -> Log.d("Start","Activity Started")
            Lifecycle.Event.ON_RESUME -> Log.d("Resume","Activity Resume")
            Lifecycle.Event.ON_PAUSE -> Log.d("Pause","Activity Pause")
            Lifecycle.Event.ON_STOP -> {

            }
            Lifecycle.Event.ON_DESTROY -> {
                val intent = Intent(context, MusicService::class.java)
                context.stopService(intent)
            }
            Lifecycle.Event.ON_ANY -> Log.d("ANY","Activity ANY")
        }
    }
}