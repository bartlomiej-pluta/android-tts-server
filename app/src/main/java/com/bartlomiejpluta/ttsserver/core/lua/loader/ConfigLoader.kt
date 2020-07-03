package com.bartlomiejpluta.ttsserver.core.lua.loader

import android.content.Context
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction
import java.io.File

class ConfigLoader(context: Context) {
   private val configDirectory = context.getExternalFilesDir("config")

   fun loadConfig(env: Globals) {
      val configFile = File(configDirectory, "config.lua")
      val table = env.loadfile(configFile.absolutePath).call().checktable()
      env.load(ConfigLibrary(table))
   }

   class ConfigLibrary(private val table: LuaTable) : TwoArgFunction() {
      override fun call(modname: LuaValue, env: LuaValue): LuaValue {
         env.set("config", table)
         return LuaValue.NIL
      }
   }
}