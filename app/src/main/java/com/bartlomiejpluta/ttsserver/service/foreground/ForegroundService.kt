package com.bartlomiejpluta.ttsserver.service.foreground

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.bartlomiejpluta.ttsserver.core.log.service.LogService
import com.bartlomiejpluta.ttsserver.core.web.server.WebServer
import com.bartlomiejpluta.ttsserver.core.web.server.WebServerFactory
import com.bartlomiejpluta.ttsserver.service.notification.ForegroundNotificationFactory
import com.bartlomiejpluta.ttsserver.service.state.ServiceState
import dagger.android.DaggerService
import javax.inject.Inject


class ForegroundService : DaggerService() {
   private var wakeLock: PowerManager.WakeLock? = null
   private var isServiceStarted = false
   private var webServer: WebServer? = null

   @Inject
   lateinit var webServerFactory: WebServerFactory

   @Inject
   lateinit var notificationFactory: ForegroundNotificationFactory

   @Inject
   lateinit var log: LogService

   override fun onCreate() {
      super.onCreate()
      startForeground(1, notificationFactory.createForegroundNotification())
   }

   override fun onBind(intent: Intent) = null

   override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
      intent?.let {
         when (it.action) {
            START -> startService()
            STOP -> stopService()
         }
      }

      return START_STICKY
   }

   override fun onDestroy() {
      webServer?.stop()
      webServer = null
   }

   @SuppressLint("WakelockTimeout")
   private fun startService() {
      if (isServiceStarted) return
      isServiceStarted = true
      log.info(TAG, "Starting service...")
      wakeLock =
         (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
               WAKELOCK_TAG
            ).apply {
               acquire()
            }
         }
      webServer = webServerFactory.createWebServer()
      webServer?.let {
         state = ServiceState.RUNNING
         it.start()
         log.info(TAG, "Service started successfully")
      }
   }

   private fun stopService() {
      log.info(TAG, "Stopping service...")
      webServer?.stop()
      webServer = null
      wakeLock?.let {
         if (it.isHeld) {
            it.release()
         }

         stopForeground(true)
         stopSelf()
      }
      state = ServiceState.STOPPED
      log.info(TAG, "Service stopped successfully")
   }

   companion object {
      // Disclaimer: I don't know the better way
      // to check whether the service is already running
      // than to place it as a static field
      var state = ServiceState.STOPPED

      private const val TAG = "@service"
      private const val WAKELOCK_TAG = "ForegroundService::lock"
      const val START = "START"
      const val STOP = "STOP"
   }
}
