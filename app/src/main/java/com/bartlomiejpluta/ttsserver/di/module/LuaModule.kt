package com.bartlomiejpluta.ttsserver.di.module

import android.content.Context
import com.bartlomiejpluta.ttsserver.core.lua.lib.*
import com.bartlomiejpluta.ttsserver.core.lua.loader.EndpointLoader
import com.bartlomiejpluta.ttsserver.core.lua.sandbox.SandboxFactory
import com.bartlomiejpluta.ttsserver.core.tts.engine.TTSEngine
import com.bartlomiejpluta.ttsserver.core.util.NetworkUtil
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
   fun sandboxFactory(
      utilLibrary: UtilLibrary,
      serverLibrary: ServerLibrary,
      httpLibrary: HTTPLibrary,
      ttsLibrary: TTSLibrary,
      sonosLibrary: SonosLibrary
   ) =
      SandboxFactory(utilLibrary, serverLibrary, httpLibrary, ttsLibrary, sonosLibrary)

   @Provides
   @Singleton
   fun utilLibrary() = UtilLibrary()

   @Provides
   @Singleton
   fun serverLibrary(networkUtil: NetworkUtil) = ServerLibrary(networkUtil)

   @Provides
   @Singleton
   fun httpLibrary() = HTTPLibrary()

   @Provides
   @Singleton
   fun ttsLibrary(ttsEngine: TTSEngine) = TTSLibrary(ttsEngine)

   @Provides
   @Singleton
   fun sonosLibrary() = SonosLibrary()
}