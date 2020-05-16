package io.bartek.ttsserver.core.web.server

import android.content.Context
import android.content.SharedPreferences
import io.bartek.ttsserver.ui.preference.PreferenceKey
import io.bartek.ttsserver.core.sonos.queue.SonosQueue
import io.bartek.ttsserver.core.tts.engine.TTSEngine

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