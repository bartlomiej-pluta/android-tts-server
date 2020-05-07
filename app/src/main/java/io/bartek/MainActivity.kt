package io.bartek

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.*
import android.view.View
import io.bartek.service.ForegroundService

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
