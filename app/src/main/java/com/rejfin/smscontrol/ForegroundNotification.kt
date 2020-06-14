package com.rejfin.smscontrol

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class ForegroundNotification {
    fun createNotification(context:Context, channelId:String, priority: Int,text:String?) : Notification{
        val builder = createBuilder(context,channelId,priority).apply {
            setSmallIcon(R.drawable.ic_mail)
            setOngoing(true)
            setContentText(text)
            this.priority = priority
        }
        return builder.build()
    }

    private fun createChannel(context:Context , channelId:String, importance:Int) {
        // Create the NotificationChannel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val channel = NotificationChannel(channelId, name, importance)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createBuilder(context:Context,channelId: String,importance: Int) : NotificationCompat.Builder{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(context, channelId, importance)
            NotificationCompat.Builder(context, channelId)
        } else {
            NotificationCompat.Builder(context, channelId)
        }
    }
}