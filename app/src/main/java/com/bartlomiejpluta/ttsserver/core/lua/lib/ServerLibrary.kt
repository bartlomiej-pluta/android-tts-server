package com.bartlomiejpluta.ttsserver.core.lua.lib

import android.content.Context
import com.bartlomiejpluta.ttsserver.core.util.NetworkUtil
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua
import java.io.File

class ServerLibrary(private val context: Context, private val networkUtil: NetworkUtil) :
   TwoArgFunction() {
   override fun call(modname: LuaValue, env: LuaValue): LuaValue {
      val server = LuaValue.tableOf().apply {
         set("port", networkUtil.port)
         set("address", networkUtil.address)
         set("url", networkUtil.url)
         set("getCachedFile", CacheFileFunction(context))
      }

      env.set("server", server)

      return LuaValue.NIL
   }

   class CacheFileFunction(context: Context) : OneArgFunction() {
      private val cacheDir = context.cacheDir

      override fun call(fileName: LuaValue) = File(cacheDir, fileName.checkjstring())
         .takeIf { it.exists() }
         ?.let { CoerceJavaToLua.coerce(it) }
         ?: LuaValue.NIL

   }
}