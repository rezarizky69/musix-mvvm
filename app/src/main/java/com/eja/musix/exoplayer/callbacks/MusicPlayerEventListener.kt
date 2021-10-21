package com.eja.musix.exoplayer.callbacks

import android.widget.Toast
import com.eja.musix.exoplayer.MusicService
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player

// callback menggunakan listener untuk music service
class MusicPlayerEventListener(
    private val musicService: MusicService
) : Player.EventListener {

    // terjadi ketika ada perubahan data di music player
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        // jika player sudah ready tapi belum di play
        if (playbackState == Player.STATE_READY && !playWhenReady) {
            // maka set stop foreground ke false
            musicService.stopForeground(false)
        }
    }

    // terjadi ketika ada error
    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
        // munculkan toast
        Toast.makeText(musicService, "An unknown error occured", Toast.LENGTH_LONG).show()
    }
}