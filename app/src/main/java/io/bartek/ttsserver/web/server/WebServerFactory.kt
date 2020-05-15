package io.bartek.ttsserver.web.server

import android.content.Context
import android.content.SharedPreferences
import io.bartek.ttsserver.preference.PreferenceKey
import io.bartek.ttsserver.sonos.SonosQueue
import io.bartek.ttsserver.tts.engine.TTSEngine

class WebServerFactory(
   private val preferences: SharedPreferences,
   private val context: Context,
   private val tts: TTSEngine,
   private val sonos: SonosQueue
) {
   fun createWebServer() =
      WebServer(
         preferences.getInt(
            PreferenceKey.PORT,
            8080
         ), context, preferences, tts, sonos
      )
}