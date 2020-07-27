package com.bartlomiejpluta.ttsserver.core.lua.loader

import android.content.Context
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction
import java.io.File

class ConfigLoader(context: Context) {
   private val configFile = File(context.getExternalFilesDir("config"), "config.lua")
   private var cachedConfig: LuaTable? = null

   fun refreshConfig(env: Globals) {
      cachedConfig = env.loadfile(configFile.absolutePath).call().checktable()
   }

   fun loadConfig(env: Globals) {
      cachedConfig
         ?.let { ConfigLibrary(it) }
         ?.let { env.load(it) }
         ?: error("Config has not been refreshed before loading start")
   }

   class ConfigLibrary(private val table: LuaTable) : TwoArgFunction() {
      override fun call(modname: LuaValue, env: LuaValue): LuaValue {
         env.set("config", table)
         return LuaValue.NIL
      }
   }
}