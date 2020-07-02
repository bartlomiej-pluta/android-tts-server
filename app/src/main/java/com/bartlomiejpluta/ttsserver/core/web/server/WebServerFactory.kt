package com.bartlomiejpluta.ttsserver.core.web.server

import android.content.Context
import android.content.SharedPreferences
import com.bartlomiejpluta.ttsserver.core.lua.loader.EndpointLoader
import com.bartlomiejpluta.ttsserver.core.tts.engine.TTSEngine
import com.bartlomiejpluta.ttsserver.ui.preference.key.PreferenceKey

class WebServerFactory(
   private val preferences: SharedPreferences,
   private val context: Context,
   private val tts: TTSEngine,
   private val endpointLoader: EndpointLoader
) {
   fun createWebServer() = WebServer(
      preferences.getInt(PreferenceKey.PORT, 8080),
      context,
      preferences,
      tts,
      endpointLoader.loadEndpoints()
   )
}