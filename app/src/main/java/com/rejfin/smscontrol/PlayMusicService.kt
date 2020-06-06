package com.rejfin.smscontrol

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.*

class PlayMusicService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uriString = intent!!.getStringExtra("uri")
        val mediaPlayer = MediaPlayer.create(applicationContext,Uri.parse(uriString))

        val notify = ForegroundNotification()
            .createNotification(this,"A", NotificationManagerCompat.IMPORTANCE_LOW)
        this.startForeground(1,notify)

        CoroutineScope(Dispatchers.IO).launch {
            launch{mediaPlayer.start()}
            delay(10000)
            mediaPlayer.stop()
            mediaPlayer.release()
            stopForeground(true)
            stopSelf()
        }
        return START_NOT_STICKY
    }
}