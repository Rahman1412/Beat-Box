package com.example.musicapp.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MusicPlayerProgressBar(
    currentTime: Long,
    totalDuration: Long
) {
    val progress by animateFloatAsState(
        targetValue = if (totalDuration > 0) currentTime.toFloat() / totalDuration else 0f,
        animationSpec = tween(durationMillis = 500) // smooth animation
    )

    LinearProgressIndicator(
        progress = progress,
        modifier = Modifier.fillMaxWidth().padding(top = 15.dp).height(4.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.onPrimary
    )
}