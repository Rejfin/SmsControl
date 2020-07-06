package com.rejfin.smscontrol.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.rejfin.smscontrol.ForegroundNotification
import com.rejfin.smscontrol.R
import kotlinx.coroutines.*

class PlayMusicService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uriString = intent!!.getStringExtra("uri")
            val duration = intent.getIntExtra("duration",15)
        val mediaPlayer = MediaPlayer.create(applicationContext,Uri.parse(uriString))

        val notify = ForegroundNotification()
            .createNotification(this,"A", NotificationManagerCompat.IMPORTANCE_LOW,getString(
                R.string.playing_music
            ))
        this.startForeground(1,notify)

        CoroutineScope(Dispatchers.IO).launch {
            launch{mediaPlayer.start()}
            delay(duration*1000L)
            mediaPlayer.stop()
            mediaPlayer.release()
            stopForeground(true)
            stopSelf()
        }
        return START_NOT_STICKY
    }
}