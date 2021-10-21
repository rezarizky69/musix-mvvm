package com.eja.musix.data.remote

import com.eja.musix.data.entities.Song
import com.eja.musix.other.Constants.SONG_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// class database
class MusicDatabase {

    // inisialisasi firestore
    private val firestore = FirebaseFirestore.getInstance()
    // inisialisasi collection dari firestore
    private val songCollection = firestore.collection(SONG_COLLECTION)

    // suspend fun untuk mendapatkan semua data berupa list dari lagu
    suspend fun getAllSongs(): List<Song> {
        return try {
            songCollection.get().await().toObjects(Song::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}