package com.bartlomiejpluta.ttsserver.di.module

import android.content.Context
import com.bartlomiejpluta.ttsserver.core.lua.lib.HTTPLibrary
import com.bartlomiejpluta.ttsserver.core.lua.lib.TTSLibrary
import com.bartlomiejpluta.ttsserver.core.lua.loader.EndpointLoader
import com.bartlomiejpluta.ttsserver.core.lua.sandbox.SandboxFactory
import com.bartlomiejpluta.ttsserver.core.tts.engine.TTSEngine
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
   fun sandboxFactory(httpLibrary: HTTPLibrary, ttsLibrary: TTSLibrary) =
      SandboxFactory(httpLibrary, ttsLibrary)

   @Provides
   @Singleton
   fun httpLibrary() = HTTPLibrary()

   @Provides
   @Singleton
   fun ttsLibrary(ttsEngine: TTSEngine) = TTSLibrary(ttsEngine)
}