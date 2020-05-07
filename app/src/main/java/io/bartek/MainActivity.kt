package io.bartek

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.*
import android.view.View
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.bartek.service.ForegroundService

class MainActivity : AppCompatActivity() {
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when(intent.getStringExtra("STATE")) {
                    "STARTED" -> notifyOnStart()
                    "STOPPED" -> notifyOnStop()
                }
            }
        }
    }

    private fun notifyOnStart() {
        Toast.makeText(
            this,
            resources.getString(R.string.server_toast_service_started),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun notifyOnStop() {
        Toast.makeText(
            this,
            resources.getString(R.string.server_toast_service_stopped),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(receiver, IntentFilter("io.bartek.web.server.CHANGE_STATE"))
    }

    override fun onPause() {
        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(receiver)
        super.onPause()
    }

    fun startServer(view: View) = actionOnService(ForegroundService.START)

    fun stopServer(view: View) = actionOnService(ForegroundService.STOP)

    fun openTTSSettings(view: View) = startActivity(Intent("com.android.settings.TTS_SETTINGS"))

    private fun actionOnService(action: String) {
        Intent(this, ForegroundService::class.java).also {
            it.action = action
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
                return
            }

            startService(it)
        }
    }


}
