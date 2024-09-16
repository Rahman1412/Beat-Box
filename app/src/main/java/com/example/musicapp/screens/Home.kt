package com.example.musicapp.screens

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState

@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Home(){
    val context = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(android.Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            listOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    )

    val requestPermission : () -> Unit = {
        permissionState.launchMultiplePermissionRequest()
    }

    LaunchedEffect(permissionState) {
        requestPermission()
    }

    when{
        permissionState.allPermissionsGranted -> {
            MyMusics()
        }
        permissionState.shouldShowRationale-> {
            RequestAccess(requestPermission)
        }
        else -> {
            RequestAccess(requestPermission)
        }
    }
}