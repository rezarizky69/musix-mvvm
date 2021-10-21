package com.eja.musix.exoplayer.callbacks

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.os.ResultReceiver
import com.eja.musix.exoplayer.FirebaseMusicSource
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

// callback untuk menyiapkan music playback
class MusicPlaybackPreparer(
    // mengambil parameter dari music source di firebase
    private val firebaseMusicSource: FirebaseMusicSource,
    // mengambil parameter berupa variabel player yg sudah disiapkan
    // dengan tipe mediametadatacompat
    private val playerPrepared: (MediaMetadataCompat?) -> Unit
) : MediaSessionConnector.PlaybackPreparer {

    // saat dalam kondisi dalam perintah
    override fun onCommand(
        // player yg digunakan
        player: Player,
        // dispatcher yg menerima perubahan ke player
        controlDispatcher: ControlDispatcher,
        // nama command
        command: String,
        // optional parameter, bisa jadi null
        extras: Bundle?,
        // result receiver yg digunakan
        cb: android.os.ResultReceiver?
    ): Boolean = false

    // mengembalikan action dari class playback preparer
    override fun getSupportedPrepareActions(): Long {
        // dapat menyiapkan dan memutar lagu dari command id
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
    }

    // saat disiapkan
    override fun onPrepare(playWhenReady: Boolean) = Unit

    // saat menyiapkan dari media id
    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        firebaseMusicSource.whenReady {
            // mencocokkan media id dari function ini dengan media id dari song di firebase
            // music source
            val itemToPlay = firebaseMusicSource.songs.find { mediaId == it.description.mediaId }
            // mengisi variabel player prepared dengan hasil pencocokkan sebelumnya
            playerPrepared(itemToPlay)
        }
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit
}