package com.vktask2.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhotoDto(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    @SerialName("download_url") val downloadUrl: String,
    val url: String
)

data class Photo(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val downloadUrl: String,
    val webUrl: String
) {
    val aspectRatio: Float = if (height == 0) 1f else width.toFloat() / height.toFloat()
}

fun PhotoDto.toDomain(): Photo = Photo(
    id = id,
    author = author,
    width = width,
    height = height,
    downloadUrl = downloadUrl,
    webUrl = url
)

