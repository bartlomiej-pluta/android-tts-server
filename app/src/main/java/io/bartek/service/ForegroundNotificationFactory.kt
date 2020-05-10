package io.bartek.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import io.bartek.MainActivity
import io.bartek.R

class ForegroundNotificationFactory(private val context: Context) {
    private val oreo: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    fun createForegroundNotification(port: Int): Notification {
        createNotificationChannel()

        val pendingIntent = createPendingIntent()

        return buildNotification(port, pendingIntent)
    }

    @Suppress("DEPRECATION")
    private fun buildNotification(port: Int, pendingIntent: PendingIntent?) =
        provideNotificationBuilder()
            .setContentTitle(context.resources.getString(R.string.service_notification_title))
            .setContentText(context.resources.getString(R.string.service_notification_text, port))
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_foreground_service)
            .setTicker(context.getString(R.string.service_notification_text))
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()

    @SuppressLint("NewApi")
    private fun createNotificationChannel() {
        if (oreo) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                context.resources.getString(R.string.service_notification_category_name),
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description =
                    context.resources.getString(R.string.service_notification_category_description)
                it
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun createPendingIntent() =
        Intent(context, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(context, 0, notificationIntent, 0)
        }

    @Suppress("DEPRECATION")
    @SuppressLint("NewApi")
    private fun provideNotificationBuilder() =
        if (oreo) Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
        else Notification.Builder(context)

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "TTSService.NOTIFICATION_CHANNEL"
    }
}