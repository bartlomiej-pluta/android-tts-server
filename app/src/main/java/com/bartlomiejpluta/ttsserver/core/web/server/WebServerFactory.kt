package com.bartlomiejpluta.ttsserver.core.web.server

import android.content.Context
import android.content.SharedPreferences
import com.bartlomiejpluta.ttsserver.core.sonos.queue.SonosQueue
import com.bartlomiejpluta.ttsserver.core.tts.engine.TTSEngine
import com.bartlomiejpluta.ttsserver.ui.preference.PreferenceKey

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