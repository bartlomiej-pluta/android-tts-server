package io.bartek.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.preference.PreferenceManager
import io.bartek.MainActivity
import io.bartek.R
import io.bartek.web.TTSServer
import java.lang.Integer.parseInt


class ForegroundService : Service() {
    private lateinit var preferences: SharedPreferences
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private var ttsServer: TTSServer? = null
    private val port: Int
        get() = parseInt(preferences.getString("preference_port", "8080")!!)

    override fun onCreate() {
        super.onCreate()
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        startForeground(1, createNotification())
    }

    private fun createNotification(): Notification {
        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                resources.getString(R.string.service_notification_category_name),
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = resources.getString(R.string.service_notification_category_description)
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            NOTIFICATION_CHANNEL_ID
        ) else Notification.Builder(this)

        return builder
            .setContentTitle(resources.getString(R.string.service_notification_title))
            .setContentText(resources.getString(R.string.service_notification_text, port))
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Ticker text")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    override fun onBind(intent: Intent) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                START -> startService()
                STOP -> stopService()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        ttsServer = null
    }

    private fun startService() {
        if(isServiceStarted) return
        isServiceStarted = true
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WebService::lock").apply {
                    acquire()
                }
            }
        ttsServer = TTSServer(port, this)
        state = ServiceState.RUNNING
    }

    private fun stopService() {
        ttsServer?.stop()
        ttsServer = null
        wakeLock?.let {
            if(it.isHeld) {
                it.release()
            }

            stopForeground(true)
            stopSelf()
        }
        state = ServiceState.STOPPED
    }

    companion object {
        // Disclaimer: I don't know the better way
        // to check whether the service is already running
        // than to place it as a static field
        var state = ServiceState.STOPPED
        private const val NOTIFICATION_CHANNEL_ID = "TTSService.NOTIFICATION_CHANNEL"
        const val PORT = "TTSService.PORT"
        const val START = "START"
        const val STOP = "STOP"
    }
}
