package com.bartlomiejpluta.ttsserver.core.lua.lib

import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction

class ThreadLibrary : TwoArgFunction() {
   override fun call(modname: LuaValue, env: LuaValue): LuaValue {
      val thread = LuaValue.tableOf().apply {
         set("sleep", SleepFunction())
      }

      env.set("thread", thread)

      return LuaValue.NIL
   }

   class SleepFunction : OneArgFunction() {
      override fun call(ms: LuaValue): LuaValue {
         Thread.sleep(ms.checklong())
         return LuaValue.NIL
      }
   }
}