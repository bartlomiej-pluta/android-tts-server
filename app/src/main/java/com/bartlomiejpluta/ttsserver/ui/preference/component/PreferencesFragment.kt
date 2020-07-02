package com.bartlomiejpluta.ttsserver.ui.preference.component

import android.app.TimePickerDialog
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
import com.bartlomiejpluta.ttsserver.core.web.mime.MimeType
import com.bartlomiejpluta.ttsserver.service.foreground.ForegroundService
import com.bartlomiejpluta.ttsserver.service.state.ServiceState
import com.bartlomiejpluta.ttsserver.ui.main.MainActivity
import com.bartlomiejpluta.ttsserver.ui.preference.custom.IntEditTextPreference
import com.bartlomiejpluta.ttsserver.ui.preference.key.PreferenceKey
import com.bartlomiejpluta.ttsserver.ui.preference.model.TimeRange


class PreferencesFragment : PreferenceFragmentCompat() {
   private lateinit var portPreference: IntEditTextPreference
   private lateinit var sayEndpointPreference: SwitchPreference
   private lateinit var fileEndpointPreference: SwitchPreference
   private lateinit var sonosEndpointPreference: SwitchPreference
   private lateinit var httpDebugPreference: SwitchPreference
   private lateinit var enableGongPreference: SwitchPreference
   private lateinit var ttsEnginePreference: Preference
   private lateinit var enableSonosSilenceScheduler: SwitchPreference
   private lateinit var enableSpeakersSilenceScheduler: SwitchPreference
   private lateinit var clearSonosCachePreference: Preference

   private val receiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
         (intent?.getStringExtra(MainActivity.STATE) ?: ServiceState.STOPPED.name)
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
      sayEndpointPreference = findPreference(PreferenceKey.ENABLE_SAY_ENDPOINT)!!
      fileEndpointPreference = findPreference(PreferenceKey.ENABLE_FILE_ENDPOINTS)!!
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
      enableSpeakersSilenceScheduler = findPreference(PreferenceKey.ENABLE_SPEAKERS_SILENCE_SCHEDULER)!!
      enableSpeakersSilenceScheduler.setOnPreferenceClickListener { preference ->
         if(!enableSpeakersSilenceScheduler.isChecked) {
            return@setOnPreferenceClickListener true
         }
         enableSpeakersSilenceScheduler.isChecked = false

         val schedule = preference
            .sharedPreferences
            .getString(
               PreferenceKey.SPEAKERS_SILENCE_SCHEDULE,
               DEFAULT_SCHEDULE
            )!!
            .let {
               TimeRange.parse(
                  it
               )
            }

         TimePickerDialog(context, { _, newBeginHour, newBeginMinute ->
            TimePickerDialog(context, { _, newEndHour, newEndMinute ->
               preference.sharedPreferences.edit()?.let { editor ->
                  val newSchedule =
                     TimeRange(
                        newBeginHour,
                        newBeginMinute,
                        newEndHour,
                        newEndMinute
                     )
                  editor.putString(PreferenceKey.SPEAKERS_SILENCE_SCHEDULE, newSchedule.toString())
                  editor.apply()
                  enableSpeakersSilenceScheduler.isChecked = true
               }
            }, schedule.endHour, schedule.endMinute, true).show()
         }, schedule.beginHour, schedule.beginMinute, true).show()

         true
      }
      enableSonosSilenceScheduler = findPreference(PreferenceKey.ENABLE_SONOS_SILENCE_SCHEDULER)!!
      enableSonosSilenceScheduler.setOnPreferenceClickListener { preference ->
         if(!enableSonosSilenceScheduler.isChecked) {
            return@setOnPreferenceClickListener true
         }
         enableSonosSilenceScheduler.isChecked = false

         val schedule = preference
            .sharedPreferences
            .getString(
               PreferenceKey.SONOS_SILENCE_SCHEDULE,
               DEFAULT_SCHEDULE
            )!!
            .let {
               TimeRange.parse(
                  it
               )
            }

         TimePickerDialog(context, { _, newBeginHour, newBeginMinute ->
            TimePickerDialog(context, { _, newEndHour, newEndMinute ->
               preference.sharedPreferences.edit()?.let { editor ->
                  val newSchedule =
                     TimeRange(
                        newBeginHour,
                        newBeginMinute,
                        newEndHour,
                        newEndMinute
                     )
                  editor.putString(PreferenceKey.SONOS_SILENCE_SCHEDULE, newSchedule.toString())
                  editor.apply()
                  enableSonosSilenceScheduler.isChecked = true
               }
            }, schedule.endHour, schedule.endMinute, true).show()
         }, schedule.beginHour, schedule.beginMinute, true).show()

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
            .apply { type = MimeType.WAV.mimeType }
            .let { Intent.createChooser(it, getString(R.string.preference_gong_picker_prompt)) }

         startActivityForResult(intent,
            PICKFILE_RESULT_CODE
         )
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
      private val DEFAULT_SCHEDULE = "22:00-07:00"
   }
}