package com.bartlomiejpluta.ttsserver.core.lua.lib

import android.content.Context
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua
import java.io.File

class CacheLibrary(private val context: Context) : TwoArgFunction() {
   override fun call(modname: LuaValue, env: LuaValue): LuaValue {
      val cache = LuaValue.tableOf().apply {
         set("file", FileFunction(context))
      }

      env.set("cache", cache)

      return LuaValue.NIL
   }

   class FileFunction(private val context: Context) : OneArgFunction() {
      override fun call(name: LuaValue) = File(context.cacheDir, name.checkjstring())
         .let { CoerceJavaToLua.coerce(it) }
   }
}