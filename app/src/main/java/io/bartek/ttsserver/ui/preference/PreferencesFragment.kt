package io.bartek.ttsserver.ui.preference

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import io.bartek.R
import io.bartek.ttsserver.service.foreground.ForegroundService
import io.bartek.ttsserver.service.state.ServiceState

class PreferencesFragment : PreferenceFragmentCompat() {
   private lateinit var portPreference: IntEditTextPreference
   private lateinit var sayEndpointPreference: SwitchPreference
   private lateinit var waveEndpointPreference: SwitchPreference
   private lateinit var sonosEndpointPreference: SwitchPreference
   private lateinit var ttsEnginePreference: Preference
   private lateinit var clearSonosCachePreference: Preference

   private val receiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
         (intent?.getStringExtra(ForegroundService.STATE) ?: ServiceState.STOPPED.name)
            .let { ServiceState.valueOf(it) }
            .let { updateViewAccordingToServiceState(it) }
      }
   }

   private fun updateViewAccordingToServiceState(state: ServiceState) {
      portPreference.isEnabled = state == ServiceState.STOPPED
      sonosEndpointPreference.isEnabled = state == ServiceState.STOPPED
   }

   override fun onResume() {
      super.onResume()
      LocalBroadcastManager
         .getInstance(requireContext())
         .registerReceiver(receiver, IntentFilter(ForegroundService.CHANGE_STATE))
      updateViewAccordingToServiceState(ForegroundService.state)
   }

   override fun onPause() {
      LocalBroadcastManager
         .getInstance(requireContext())
         .unregisterReceiver(receiver)
      super.onPause()
   }

   override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResource(R.xml.preferences, rootKey)
      portPreference = findPreference(PreferenceKey.PORT)!!
      portPreference.setOnBindEditTextListener { it.inputType = InputType.TYPE_CLASS_NUMBER }
      sayEndpointPreference = findPreference(PreferenceKey.ENABLE_SAY_ENDPOINT)!!
      waveEndpointPreference = findPreference(PreferenceKey.ENABLE_WAVE_ENDPOINT)!!
      sonosEndpointPreference = findPreference(PreferenceKey.ENABLE_SONOS_ENDPOINT)!!
      ttsEnginePreference = findPreference(PreferenceKey.TTS)!!
      ttsEnginePreference.setOnPreferenceClickListener {
         startActivity(Intent(ANDROID_TTS_SETTINGS))
         true
      }
      clearSonosCachePreference = findPreference(PreferenceKey.INVALIDATE_SONOS_CACHE)!!
      clearSonosCachePreference.setOnPreferenceClickListener {
         context?.cacheDir?.listFiles() ?. forEach { it.delete() }
         Toast.makeText(context, getString(R.string.preference_invalidate_sonos_cache_toast), Toast.LENGTH_SHORT).show()
         true
      }
      updateViewAccordingToServiceState(ForegroundService.state)
   }

   companion object {
      private const val ANDROID_TTS_SETTINGS = "com.android.settings.TTS_SETTINGS"
   }
}