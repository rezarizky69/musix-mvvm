package com.eja.musix.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.session.MediaController
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.eja.musix.R
import com.eja.musix.other.Constants.NOTIFICATION_CHANNEL_ID
import com.eja.musix.other.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

// class untuk memanage notifikasi dari lagu
class MusicNotificationManager(
    private val context: Context,
    // token dari media session
    sessionToken: MediaSessionCompat.Token,
    // listener yg dibuat dari base manager
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val newSongCallback: () -> Unit
) {

    // basa manager dari notifikasi
    private val notificationManager: PlayerNotificationManager

    // function yg dijalankan pertama kali
    init {
        // inisialisasi controller dengan mengambil parameter context dan token dari media session
        val mediaController = MediaControllerCompat(context, sessionToken)
        // membuat channel notifikasi
        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context,
            NOTIFICATION_CHANNEL_ID,
            R.string.notification_channel_name,
            R.string.notification_channel_description,
            NOTIFICATION_ID,
            // adapter untuk komponen yg akan ditampilkan ke notifikasi
            DescriptionAdapter(mediaController),
            notificationListener
        ).apply {
            setSmallIcon(R.drawable.ic_music)
            setMediaSessionToken(sessionToken)
        }
    }

    // function untuk menampilkan notification dengan set media player
    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }

    // adapter yg akan dipakai yg berisi apa saja yg akan ditampilkan di notifikasi
    private inner class DescriptionAdapter(
        // controller yg dipakai agar bisa mengambil informasi dari lagu
        private val mediaController: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {
        // mengambil judul lagu
        override fun getCurrentContentTitle(player: Player): CharSequence {
            newSongCallback()
            return mediaController.metadata.description.title.toString()
        }

        // mengambil activity yg akan dijalankan ketika tap notification
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        // mengambil subtitle lagu
        override fun getCurrentContentText(player: Player): CharSequence? {
            return mediaController.metadata.description.subtitle.toString()
        }

        // set icon untuk notifikasi yg akan ditampilkan
        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            Glide.with(context).asBitmap()
                .load(mediaController.metadata.description.iconUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        callback.onBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit
                })
            return null

        }

    }


}