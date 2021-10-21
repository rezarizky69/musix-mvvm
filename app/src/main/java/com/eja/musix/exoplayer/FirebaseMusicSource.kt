package com.eja.musix.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.eja.musix.data.remote.MusicDatabase
import com.eja.musix.exoplayer.State.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSource @Inject constructor(
    // parameter dari database
    private val musicDatabase: MusicDatabase
) {

    // list dari lagu initial nya list kosong yg berisi metadata dari lagu yg akan dipakai
    var songs = emptyList<MediaMetadataCompat>()

    // function untuk fetch data lagu menggunakan coroutine
    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        // dilakukan saat state initializing
        state = STATE_INITIALIZING
        // membuat dan mengisi variable allSongs dengan function dari music database
        val allSongs = musicDatabase.getAllSongs()
        // mapping data dari lagu yg didapatkan dari allSongs ke list lagu songs
        // yg berisi metadata yg sebelumnya sudah dibuat
        songs = allSongs.map { song ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, song.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, song.mediaId)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.imageUrl)
                .putString(METADATA_KEY_MEDIA_URI, song.songUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.imageUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.subtitle)
                .build()
        }
        // jika sudah, maka set state ke initialized
        state = STATE_INITIALIZED
    }

    // menggabungkan list songs yg kosong dan mengisinya dengan song berdasarkan media uri
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    // mengubah hasil dari metadata lagu dengan mapping menjadi satu item lagu
    // dengan deskripsi sebagai berikut
    // dimana hasil dari mapping akan ditampilkan ke user
    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()

    // listener yg berisi list dari boolean
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    // saat state created
    private var state: State = STATE_CREATED
        // set nilainya
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                // dijalankan secara synchronous
                synchronized(onReadyListeners) {
                    // field yg lama di isi dengan value yg baru
                    field = value
                    // untuk setiap list di listener
                    onReadyListeners.forEach { listener ->
                        // ubah state nya menjadi initialized
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    // function yg dijalankan ketika kondisi sudah siap
    fun whenReady(action: (Boolean) -> Unit): Boolean {
        if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            return false
        } else {
            action(state == STATE_INITIALIZED)
            return true
        }
    }
}

// enum state apa saja yg dibutuhkan
enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}