package io.bartek.ttsserver.di.module

import android.content.Context
import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import io.bartek.ttsserver.service.notification.ForegroundNotificationFactory
import io.bartek.ttsserver.core.sonos.queue.SonosQueue
import io.bartek.ttsserver.core.tts.engine.TTSEngine
import io.bartek.ttsserver.core.tts.status.TTSStatusHolder
import io.bartek.ttsserver.core.util.NetworkUtil
import io.bartek.ttsserver.core.web.server.WebServerFactory
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
   fun tts(context: Context, textToSpeech: TextToSpeech, ttsStatusHolder: TTSStatusHolder) =
      TTSEngine(context, textToSpeech, ttsStatusHolder)

   @Provides
   @Singleton
   fun webServerFactory(
      preferences: SharedPreferences,
      context: Context,
      tts: TTSEngine,
      sonos: SonosQueue
   ) =
      WebServerFactory(
         preferences,
         context,
         tts,
         sonos
      )

   @Provides
   @Singleton
   fun preferences(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

   @Provides
   @Singleton
   fun networkUtil(context: Context) = NetworkUtil(context)

   @Provides
   @Singleton
   fun sonosQueue(tts: TTSEngine, networkUtil: NetworkUtil, preferences: SharedPreferences) =
      SonosQueue(tts, networkUtil, preferences)

   @Provides
   @Singleton
   fun foregroundNotificationFactory(context: Context) =
      ForegroundNotificationFactory(
         context
      )
}