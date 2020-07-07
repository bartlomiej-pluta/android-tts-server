package com.bartlomiejpluta.ttsserver.core.lua.sandbox

import android.content.Context
import com.bartlomiejpluta.ttsserver.core.lua.lib.*
import com.bartlomiejpluta.ttsserver.core.lua.loader.ConfigLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.luaj.vm2.Globals
import org.luaj.vm2.LoadState
import org.luaj.vm2.compiler.LuaC
import org.luaj.vm2.lib.PackageLib
import org.luaj.vm2.lib.StringLib
import org.luaj.vm2.lib.TableLib
import org.luaj.vm2.lib.jse.JseBaseLib
import org.luaj.vm2.lib.jse.JseMathLib
import org.luaj.vm2.lib.jse.JseOsLib

class SandboxFactory(
   private val context: Context,
   private val configLoader: ConfigLoader,
   private val threadLibrary: ThreadLibrary,
   private val serverLibrary: ServerLibrary,
   private val httpLibrary: HTTPLibrary,
   private val ttsLibrary: TTSLibrary,
   private val sonosLibrary: SonosLibrary
) {
   fun createSandbox() = runBlocking {
      withContext(Dispatchers.Default) {
         createLuaGlobals()
      }
   }

   private fun createLuaGlobals() = Globals().also { sandbox ->
      loadStandardLibraries(sandbox)
      loadApplicationLibraries(sandbox)
      install(sandbox)
      loadLuaLibraries(sandbox)
   }

   private fun loadStandardLibraries(sandbox: Globals) {
      sandbox.load(JseBaseLib())
      sandbox.load(PackageLib())
      sandbox.load(TableLib())
      sandbox.load(StringLib())
      sandbox.load(JseMathLib())
      sandbox.load(JseOsLib())
   }

   private fun loadApplicationLibraries(sandbox: Globals) {
      sandbox.load(serverLibrary)
      sandbox.load(threadLibrary)
      sandbox.load(httpLibrary)
      sandbox.load(ttsLibrary)
      sandbox.load(sonosLibrary)
   }

   private fun install(sandbox: Globals) {
      LoadState.install(sandbox)
      LuaC.install(sandbox)
      configLoader.loadConfig(sandbox)
   }

   private fun loadLuaLibraries(sandbox: Globals) {
      context.assets.list("lua")
         ?.map { it to context.assets.open("lua/$it") }
         ?.map { (name, stream) -> name to stream.bufferedReader() }
         ?.map { (name, reader) -> name.substringBeforeLast(".") to sandbox.load(reader, name) }
         ?.forEach { (name, value) -> sandbox.set(name, value.call()) }
   }
}