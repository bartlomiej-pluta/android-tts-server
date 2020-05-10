package io.bartek.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.PowerManager
import androidx.preference.PreferenceManager
import io.bartek.preference.PreferenceKey
import io.bartek.web.TTSServer


class ForegroundService : Service() {
    private lateinit var preferences: SharedPreferences
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private var ttsServer: TTSServer? = null
    private val port: Int
        get() = preferences.getInt(PreferenceKey.PORT, 8080)
    private val notificationFactory = ForegroundNotificationFactory(this)

    override fun onCreate() {
        super.onCreate()
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        startForeground(1, notificationFactory.createForegroundNotification(port))
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
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG).apply {
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

        private const val WAKELOCK_TAG = "ForegroundService::lock"
        const val CHANGE_STATE = "io.bartek.service.CHANGE_STATE"
        const val STATE = "STATE"
        const val START = "START"
        const val STOP = "STOP"
    }
}
