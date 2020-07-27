package com.bartlomiejpluta.ttsserver.di.module

import android.content.Context
import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import androidx.preference.PreferenceManager
import com.bartlomiejpluta.ttsserver.core.lua.loader.EndpointLoader
import com.bartlomiejpluta.ttsserver.core.tts.engine.TTSEngine
import com.bartlomiejpluta.ttsserver.core.tts.status.TTSStatusHolder
import com.bartlomiejpluta.ttsserver.core.util.AudioConverter
import com.bartlomiejpluta.ttsserver.core.util.NetworkUtil
import com.bartlomiejpluta.ttsserver.core.web.server.WebServerFactory
import com.bartlomiejpluta.ttsserver.service.notification.ForegroundNotificationFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TTSModule {

   @Provides
   @Singleton
   fun ttsStatusHolder() = TTSStatusHolder()

   @Provides
   @Singleton
   fun textToSpeech(context: Context, ttsStatusHolder: TTSStatusHolder) =
      TextToSpeech(context, ttsStatusHolder)

   @Provides
   @Singleton
   fun tts(
      context: Context,
      textToSpeech: TextToSpeech,
      ttsStatusHolder: TTSStatusHolder,
      preferences: SharedPreferences,
      converter: AudioConverter
   ) = TTSEngine(context, textToSpeech, ttsStatusHolder, preferences, converter)

   @Provides
   @Singleton
   fun webServerFactory(
      preferences: SharedPreferences,
      context: Context,
      tts: TTSEngine,
      endpointLoader: EndpointLoader
   ) = WebServerFactory(
      preferences,
      context,
      tts,
      endpointLoader
   )

   @Provides
   @Singleton
   fun preferences(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

   @Provides
   @Singleton
   fun networkUtil(context: Context, preferences: SharedPreferences) =
      NetworkUtil(context, preferences)

   @Provides
   @Singleton
   fun audioConverter(context: Context) = AudioConverter(context)

   @Provides
   @Singleton
   fun foregroundNotificationFactory(
      context: Context,
      networkUtil: NetworkUtil
   ) = ForegroundNotificationFactory(context, networkUtil)
}