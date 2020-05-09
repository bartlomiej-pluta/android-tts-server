package io.bartek

import android.content.*
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.bartek.preference.PreferencesActivity
import io.bartek.service.ForegroundService
import io.bartek.service.ServiceState


class MainActivity : AppCompatActivity() {
    private lateinit var controlServerButton: Button

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                updateViewAccordingToServiceState(
                    ServiceState.valueOf(it.getStringExtra("STATE") ?: "STOPPED")
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.open_preferences -> startActivity(Intent(this, PreferencesActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

    private fun updateViewAccordingToServiceState(newState: ServiceState) {
        controlServerButton.isEnabled = true
        when (newState) {
            ServiceState.STOPPED -> controlServerButton.text = getString(R.string.main_activity_run)
            ServiceState.RUNNING -> controlServerButton.text = getString(R.string.main_activity_stop)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        controlServerButton = findViewById(R.id.control_server_button)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(receiver, IntentFilter("io.bartek.web.server.CHANGE_STATE"))
        updateViewAccordingToServiceState(ForegroundService.state)
    }

    override fun onPause() {
        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(receiver)
        super.onPause()
    }

    fun controlServer(view: View) {
        controlServerButton.isEnabled = false
        when (ForegroundService.state) {
            ServiceState.STOPPED -> actionOnService(ForegroundService.START)
            ServiceState.RUNNING -> actionOnService(ForegroundService.STOP)
        }
    }

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
