package com.bartlomiejpluta.ttsserver.di.module

import android.content.Context
import android.content.SharedPreferences
import com.bartlomiejpluta.ttsserver.core.lua.lib.*
import com.bartlomiejpluta.ttsserver.core.lua.loader.ConfigLoader
import com.bartlomiejpluta.ttsserver.core.lua.loader.EndpointLoader
import com.bartlomiejpluta.ttsserver.core.lua.sandbox.SandboxFactory
import com.bartlomiejpluta.ttsserver.core.tts.engine.TTSEngine
import com.bartlomiejpluta.ttsserver.core.util.NetworkUtil
import com.bartlomiejpluta.ttsserver.initializer.ScriptsInitializer
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LuaModule {

   @Provides
   @Singleton
   fun endpointLoader(context: Context, sandboxFactory: SandboxFactory) =
      EndpointLoader(context, sandboxFactory)

   @Provides
   @Singleton
   fun configLoader(context: Context) = ConfigLoader(context)

   @Provides
   @Singleton
   fun sandboxFactory(
      context: Context,
      configLoader: ConfigLoader,
      debugLibrary: DebugLibrary,
      threadLibrary: ThreadLibrary,
      serverLibrary: ServerLibrary,
      httpLibrary: HTTPLibrary,
      ttsLibrary: TTSLibrary,
      sonosLibrary: SonosLibrary
   ) = SandboxFactory(
      context,
      configLoader,
      debugLibrary,
      threadLibrary,
      serverLibrary,
      httpLibrary,
      ttsLibrary,
      sonosLibrary
   )

   @Provides
   @Singleton
   fun debugLibrary(context: Context) = DebugLibrary(context)

   @Provides
   @Singleton
   fun threadLibrary() = ThreadLibrary()

   @Provides
   @Singleton
   fun serverLibrary(context: Context, networkUtil: NetworkUtil) =
      ServerLibrary(context, networkUtil)

   @Provides
   @Singleton
   fun httpLibrary() = HTTPLibrary()

   @Provides
   @Singleton
   fun ttsLibrary(ttsEngine: TTSEngine) = TTSLibrary(ttsEngine)

   @Provides
   @Singleton
   fun sonosLibrary() = SonosLibrary()

   @Provides
   @Singleton
   fun scriptsInitializer(context: Context, preferences: SharedPreferences) =
      ScriptsInitializer(context, preferences)
}