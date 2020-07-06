package com.bartlomiejpluta.ttsserver.core.lua.lib

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bartlomiejpluta.R
import com.bartlomiejpluta.ttsserver.ui.main.MainActivity
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction

class DebugLibrary(private val context: Context) : TwoArgFunction() {
   override fun call(modname: LuaValue, env: LuaValue): LuaValue {
      env.set("debug", DebugFunction(context))

      return LuaValue.NIL
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