package com.eja.musix.data.entities

// data class yang mewakili struktur dari model lagu
data class Song(
    // id yg mewakili representasi tiap lagu
    val mediaId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val songUrl: String = "",
    val imageUrl: String = ""
)