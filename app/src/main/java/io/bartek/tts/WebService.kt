package io.bartek.tts

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.telephony.ServiceState
import android.util.Log
import android.widget.Toast
import io.bartek.MainActivity
import io.bartek.R
import io.bartek.web.TTSServer

class WebService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private var ttsServer: TTSServer? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("TTSService", "Service has been created")
        startForeground(1, createNotification())
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "HTTP SERVER CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                notificationChannelId,
                "HTTP Server",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "HTTP Server channel with keeps the service running"
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
            notificationChannelId
        ) else Notification.Builder(this)

        return builder
            .setContentTitle("Endless Service")
            .setContentText("This is your favorite endless service working")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Ticker text")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d("TTSService", "Something is willing to bind the service")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TTSService", "onStartCommand with startId: $startId")
        intent?.let {
            when(it.action) {
                START -> startService(it.getIntExtra(PORT, 8080))
                STOP -> stopService()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        ttsServer = null
    }

    private fun startService(port: Int) {
        if(isServiceStarted) return
        Log.d("TTSService", "Starting service...")
        isServiceStarted = true
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }
        ttsServer = TTSServer(port, this)
    }

    private fun stopService() {
        Log.d("TTSService", "Stopping service...")
        ttsServer?.stop()
        ttsServer = null
        wakeLock?.let {
            if(it.isHeld) {
                it.release()
            }

            stopForeground(true)
            stopSelf()
        }
    }

    companion object {
        const val PORT = "TTSService.PORT"
        const val START = "START"
        const val STOP = "STOP"
    }
}
