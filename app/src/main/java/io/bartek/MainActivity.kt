package io.bartek

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.bartek.help.HelpActivity
import io.bartek.preference.PreferencesActivity
import io.bartek.service.ForegroundService
import io.bartek.service.ServiceState


class MainActivity : AppCompatActivity() {
    private lateinit var controlServerButton: AppCompatImageButton
    private lateinit var promptText: TextView

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
            R.id.open_help -> startActivity(Intent(this, HelpActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

    private fun updateViewAccordingToServiceState(newState: ServiceState) {
        controlServerButton.isEnabled = true
        when (newState) {
            ServiceState.STOPPED -> {
                controlServerButton.setImageResource(R.drawable.ic_power_off)
                promptText.text = getString(R.string.main_activity_prompt_to_run)
            }
            ServiceState.RUNNING -> {
                controlServerButton.setImageResource(R.drawable.ic_power_on)
                promptText.text = getString(R.string.main_activity_prompt_to_stop)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        controlServerButton = findViewById(R.id.control_server_button)
        promptText = findViewById(R.id.prompt_text)
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
