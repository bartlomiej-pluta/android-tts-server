package com.bartlomiejpluta.ttsserver.core.lua.lib

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bartlomiejpluta.ttsserver.R
import com.bartlomiejpluta.ttsserver.core.util.NetworkUtil
import com.bartlomiejpluta.ttsserver.ui.main.MainActivity
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
         set("debug", DebugFunction(context))
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

   class DebugFunction(private val context: Context) : OneArgFunction() {
      override fun call(arg: LuaValue): LuaValue {
         LocalBroadcastManager
            .getInstance(context)
            .sendBroadcast(Intent(MainActivity.POPUP).apply {
               putExtra(MainActivity.TITLE, context.resources.getString(R.string.debug))
               putExtra(MainActivity.MESSAGE, arg.toString())
            })

         return LuaValue.NIL
      }
   }
}