package com.eja.musix.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.eja.musix.exoplayer.callbacks.MusicPlaybackPreparer
import com.eja.musix.exoplayer.callbacks.MusicPlayerEventListener
import com.eja.musix.exoplayer.callbacks.MusicPlayerNotificationListener
import com.eja.musix.other.Constants.MEDIA_ROOT_ID
import com.eja.musix.other.Constants.NETWORK_ERROR
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

// nama constant untuk music service
private const val SERVICE_TAG = "MusicService"


// class untuk music service
@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    // inject untuk data source
    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    // inject untuk exoplayer sebagai music player
    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    // inject untuk music source dari firebase
    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    // inisialisasi untuk notifikasi
    private lateinit var musicNotificationManager: MusicNotificationManager

    // inisialisasi coroutine untuk handle music service
    private val serviceJob = Job()
    // dilakukan di thread main
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    // inisialisasi media session dan connector yg akan dipakai
    private lateinit var mediaSession: MediaSessionCompat
    // connector yg menghubungkan media session dan player yg dipakai
    private lateinit var mediaSessionConnector: MediaSessionConnector

    // initial foreground service yg di set false
    var isForegroundService = false

    // current playing song dengan tipe mediametadatacompat dan di inisialisasi null
    private var curPlayingSong: MediaMetadataCompat? = null

    // initial bool untuk apakah player sudah di inisialisasi dengan nilai awal false
    private var isPlayerInitialized = false

    // listener untuk event di music player
    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    companion object {
        // initial durasi bagi current song dengan nilai awal 0 long
        var curSongDuration = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()
        // menjalankan dengan coroutine
        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }

        // intent yg akan dijalankan ketika notifikasi musik di tap
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        // mengaplikasikan intent yang sudah dibuat ke media session
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            // set media session ke active agar dapat digunakan
            isActive = true
        }

        // token yg akan dipakai, mengambil dari media session
        sessionToken = mediaSession.sessionToken

        // inisialisasi notifikasi menggunakan token yg telah dibuat
        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            // memanggil class listener untuk notifikasi, dengan parameter music service ini sendiri
            MusicPlayerNotificationListener(this)
        ) {
            // durasi dari lagu yg sedang diputar
            // dicocokkan dengan durasi dari exoplayer
            curSongDuration = exoPlayer.duration
        }


        // menyiapkan music playback
        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource) {
            curPlayingSong = it
            preparePlayer(
                firebaseMusicSource.songs,
                it,
                true
            )
        }

        // set media player yg dipakai menggunakan connector yg telah dibuat
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)
        musicNotificationManager.showNotification(exoPlayer)
    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val curSongIndex = if (curPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(curSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    // saat task dihapus maka stop exoplayer
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    // saat task di hentikan maka hentikan semua aktivitas yg terkait
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }

    // untuk mendapatkan akses ke root directory browser music
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    // saat mengakses child directory
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        // ketika parent id sudah cocok dengan media root id
        when (parentId) {
            MEDIA_ROOT_ID -> {
                // maka tampung disini
                val resultsSent = firebaseMusicSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(firebaseMusicSource.asMediaItems())
                        if (!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()) {
                            preparePlayer(
                                firebaseMusicSource.songs,
                                firebaseMusicSource.songs[0],
                                false
                            )
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }
                if (!resultsSent) {
                    result.detach()
                }
            }
        }
    }
}