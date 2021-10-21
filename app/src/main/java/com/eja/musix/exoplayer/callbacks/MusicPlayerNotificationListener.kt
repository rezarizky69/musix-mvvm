package com.eja.musix.exoplayer.callbacks

import android.app.Notification
import android.content.Intent
import androidx.core.content.ContextCompat
import com.eja.musix.exoplayer.MusicService
import com.eja.musix.other.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.ui.PlayerNotificationManager

// listener yg listen pada notifikasi dan akan melakukan berbagi aksi tergantung dari
// behavior nya
class MusicPlayerNotificationListener(
    // mengambil parameter dari music service
    private val musicService: MusicService
) : PlayerNotificationManager.NotificationListener {

    // ketika notifikasi di gagalkan / dismissed by user
    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
        // pada music service
        musicService.apply {
            // hentikan foreground service
            stopForeground(true)
            // set false pada service
            isForegroundService = false
            // hentikan kegiatan
            stopSelf()
        }
    }

    // ketika notifikasi diluncurkan
    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        // pada music service
        musicService.apply {
            // jika masih ongoing dan belum dimulai foreground service
            if(ongoing && !isForegroundService) {
                // set mulai foreground service
                ContextCompat.startForegroundService(
                    this,
                    // menuju activity ini
                    Intent(applicationContext, this::class.java)
                )
                // memulai foreground service dengan memakai notifikasi id
                startForeground(NOTIFICATION_ID, notification)
                // set true pada foreground service
                isForegroundService = true
            }
        }
    }
}