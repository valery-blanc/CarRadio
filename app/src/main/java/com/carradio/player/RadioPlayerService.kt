package com.carradio.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.carradio.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RadioPlayerService : MediaSessionService() {

    @Inject
    lateinit var playerController: PlayerController

    private var mediaSession: MediaSession? = null

    companion object {
        private const val CHANNEL_ID = "carradio_playback"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaSession = MediaSession.Builder(this, playerController.player).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // BUG-001 : startForegroundService() exige que startForeground() soit appelé dans les 5s.
        // Media3 appelle startForeground() seulement quand le player passe en BUFFERING/PLAYING,
        // ce qui peut dépasser ce délai. On appelle startForeground() immédiatement avec une
        // notification minimale ; Media3 la mettra à jour dès que la lecture commence.
        startForeground(NOTIFICATION_ID, buildInitialNotification())
        return START_NOT_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            // Ne pas appeler player.release() ici : le player est un singleton géré
            // par PlayerController (durée de vie = process). L'appeler ici causerait
            // un audio focus leak lors de la prochaine lecture (BUG-016).
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun buildInitialNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.app_name))
            .setSilent(true)
            .build()

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}
