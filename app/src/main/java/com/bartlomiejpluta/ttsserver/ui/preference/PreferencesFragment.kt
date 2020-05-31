package com.bartlomiejpluta.ttsserver.ui.preference

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
import com.bartlomiejpluta.R
import com.bartlomiejpluta.ttsserver.service.foreground.ForegroundService
import com.bartlomiejpluta.ttsserver.service.state.ServiceState


class PreferencesFragment : PreferenceFragmentCompat() {
   private lateinit var portPreference: IntEditTextPreference
   private lateinit var sayEndpointPreference: SwitchPreference
   private lateinit var waveEndpointPreference: SwitchPreference
   private lateinit var sonosEndpointPreference: SwitchPreference
   private lateinit var httpDebugPreference: SwitchPreference
   private lateinit var enableGongPreference: SwitchPreference
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
      enableGongPreference.isEnabled = state == ServiceState.STOPPED
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
      httpDebugPreference = findPreference(PreferenceKey.ENABLE_HTTP_DEBUG)!!
      sayEndpointPreference = findPreference(PreferenceKey.ENABLE_SAY_ENDPOINT)!!
      waveEndpointPreference = findPreference(PreferenceKey.ENABLE_WAVE_ENDPOINT)!!
      sonosEndpointPreference = findPreference(PreferenceKey.ENABLE_SONOS_ENDPOINT)!!
      enableGongPreference = findPreference(PreferenceKey.ENABLE_GONG)!!
      enableGongPreference.setOnPreferenceClickListener { preference ->
         openFilePicker(preference)

         true
      }
      ttsEnginePreference = findPreference(PreferenceKey.TTS)!!
      ttsEnginePreference.setOnPreferenceClickListener {
         startActivity(Intent(ANDROID_TTS_SETTINGS))
         true
      }
      clearSonosCachePreference = findPreference(PreferenceKey.INVALIDATE_SONOS_CACHE)!!
      clearSonosCachePreference.setOnPreferenceClickListener {
         context?.cacheDir?.listFiles()?.forEach { it.delete() }
         Toast.makeText(
            context,
            getString(R.string.preference_invalidate_sonos_cache_toast),
            Toast.LENGTH_SHORT
         ).show()
         true
      }
      updateViewAccordingToServiceState(ForegroundService.state)
   }

   private fun openFilePicker(preference: Preference?) {
      if ((preference as SwitchPreference).isChecked) {
         val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .apply { type = "audio/x-wav" }
            .let { Intent.createChooser(it, getString(R.string.preference_gong_picker_prompt)) }

         startActivityForResult(intent, PICKFILE_RESULT_CODE)
      }
   }

   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      when (requestCode) {
         PICKFILE_RESULT_CODE -> updateGongPathPreference(resultCode, data)
      }
   }

   private fun updateGongPathPreference(resultCode: Int, data: Intent?) {
      enableGongPreference.isChecked = resultCode == -1

      if (resultCode == -1) {
         enableGongPreference.sharedPreferences?.edit()?.let { editor ->
            editor.putString(PreferenceKey.GONG, data?.data?.toString())
            editor.commit()
         }
      }
   }

   companion object {
      private const val ANDROID_TTS_SETTINGS = "com.android.settings.TTS_SETTINGS"
      private const val PICKFILE_RESULT_CODE = 1
   }
}