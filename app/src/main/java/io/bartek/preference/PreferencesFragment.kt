package io.bartek.preference

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.InputType
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import io.bartek.R
import io.bartek.service.ForegroundService
import io.bartek.service.ServiceState

class PreferencesFragment : PreferenceFragmentCompat() {
    private lateinit var portPreference: IntEditTextPreference
    private lateinit var sayEndpointPreference: SwitchPreference
    private lateinit var waveEndpointPreference: SwitchPreference
    private lateinit var ttsEnginePreference: Preference

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                updateViewAccordingToServiceState(
                    ServiceState.valueOf(it.getStringExtra("STATE") ?: "STOPPED")
                )
            }
        }
    }

    private fun updateViewAccordingToServiceState(state: ServiceState) {
        portPreference.isEnabled = state == ServiceState.STOPPED
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager
            .getInstance(context!!)
            .registerReceiver(receiver, IntentFilter("io.bartek.web.server.CHANGE_STATE"))
        updateViewAccordingToServiceState(ForegroundService.state)
    }

    override fun onPause() {
        LocalBroadcastManager
            .getInstance(context!!)
            .unregisterReceiver(receiver)
        super.onPause()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        portPreference = findPreference("preference_port")!!
        portPreference.setOnBindEditTextListener { it.inputType = InputType.TYPE_CLASS_NUMBER }
        sayEndpointPreference = findPreference("preference_enable_say_endpoint")!!
        waveEndpointPreference = findPreference("preference_enable_wave_endpoint")!!
        ttsEnginePreference = findPreference("preference_tts")!!
        ttsEnginePreference.setOnPreferenceClickListener {
            startActivity(Intent("com.android.settings.TTS_SETTINGS"))
            true
        }
        updateViewAccordingToServiceState(ForegroundService.state)
    }
}