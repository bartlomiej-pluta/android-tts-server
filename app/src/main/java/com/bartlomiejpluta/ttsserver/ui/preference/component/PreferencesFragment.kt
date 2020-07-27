package com.bartlomiejpluta.ttsserver.ui.preference.component

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
import com.bartlomiejpluta.ttsserver.R
import com.bartlomiejpluta.ttsserver.service.foreground.ForegroundService
import com.bartlomiejpluta.ttsserver.service.state.ServiceState
import com.bartlomiejpluta.ttsserver.ui.main.MainActivity
import com.bartlomiejpluta.ttsserver.ui.preference.custom.IntEditTextPreference
import com.bartlomiejpluta.ttsserver.ui.preference.key.PreferenceKey


class PreferencesFragment : PreferenceFragmentCompat() {
   private lateinit var portPreference: IntEditTextPreference
   private lateinit var httpDebugPreference: SwitchPreference
   private lateinit var clearCachePreference: Preference
   private lateinit var ttsEnginePreference: Preference

   private val receiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
         (intent?.getStringExtra(MainActivity.STATE) ?: ServiceState.STOPPED.name)
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
         .getInstance(requireContext())
         .registerReceiver(receiver, IntentFilter(MainActivity.CHANGE_STATE))
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
      httpDebugPreference = findPreference(PreferenceKey.ENABLE_HTTP_DEBUG)!!
      ttsEnginePreference = findPreference(PreferenceKey.TTS)!!
      ttsEnginePreference.setOnPreferenceClickListener {
         startActivity(Intent(ANDROID_TTS_SETTINGS))
         true
      }
      clearCachePreference = findPreference(PreferenceKey.INVALIDATE_CACHE)!!
      clearCachePreference.setOnPreferenceClickListener {
         context?.cacheDir?.listFiles()?.forEach { it.delete() }
         Toast.makeText(
            context,
            getString(R.string.preference_invalidate_cache_toast),
            Toast.LENGTH_SHORT
         ).show()
         true
      }
      updateViewAccordingToServiceState(ForegroundService.state)
   }


   companion object {
      private const val ANDROID_TTS_SETTINGS = "com.android.settings.TTS_SETTINGS"
   }
}