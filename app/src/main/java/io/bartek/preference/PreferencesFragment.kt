package io.bartek.preference

import android.content.Intent
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import io.bartek.R

class PreferencesFragment : PreferenceFragmentCompat() {
    private lateinit var portPreference: EditTextPreference
    private lateinit var sayEndpointPreference: SwitchPreference
    private lateinit var waveEndpointPreference: SwitchPreference
    private lateinit var ttsEnginePreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        portPreference = findPreference("preference_port")!!
        sayEndpointPreference = findPreference("preference_enable_say_endpoint")!!
        waveEndpointPreference = findPreference("preference_enable_wave_endpoint")!!
        ttsEnginePreference = findPreference("preference_tts")!!
        ttsEnginePreference.setOnPreferenceClickListener {
            startActivity(Intent("com.android.settings.TTS_SETTINGS"))
            true
        }
    }
}