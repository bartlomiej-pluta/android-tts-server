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
         (intent?.getStringExtra(ForegroundService.STATE) ?: ServiceState.STOPPED.name)
            .let { ServiceState.valueOf(it) }
            .let { updateViewAccordingToServiceState(it) }
      }
   }

   private fun updateViewAccordingToServiceState(state: ServiceState) {
      portPreference.isEnabled = state == ServiceState.STOPPED
   }

   override fun onResume() {
      super.onResume()
      LocalBroadcastManager
         .getInstance(context!!)
         .registerReceiver(receiver, IntentFilter(ForegroundService.CHANGE_STATE))
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
      portPreference = findPreference(PreferenceKey.PORT)!!
      portPreference.setOnBindEditTextListener { it.inputType = InputType.TYPE_CLASS_NUMBER }
      sayEndpointPreference = findPreference(PreferenceKey.ENABLE_SAY_ENDPOINT)!!
      waveEndpointPreference = findPreference(PreferenceKey.ENABLE_WAVE_ENDPOINT)!!
      ttsEnginePreference = findPreference(PreferenceKey.TTS)!!
      ttsEnginePreference.setOnPreferenceClickListener {
         startActivity(Intent(ANDROID_TTS_SETTINGS))
         true
      }
      updateViewAccordingToServiceState(ForegroundService.state)
   }

   companion object {
      private const val ANDROID_TTS_SETTINGS = "com.android.settings.TTS_SETTINGS"
   }
}