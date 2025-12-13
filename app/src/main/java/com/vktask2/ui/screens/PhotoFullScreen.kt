package com.vktask2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vktask2.data.Photo

@Composable
fun PhotoFullScreen(photo: Photo) {
    val context = LocalContext.current
    val request = ImageRequest.Builder(context)
        .data(photo.downloadUrl)
        .crossfade(true)
        .build()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AsyncImage(
            model = request,
            contentDescription = photo.author,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

