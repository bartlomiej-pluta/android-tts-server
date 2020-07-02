package com.bartlomiejpluta.ttsserver.core.lua.lib

import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction

class UtilLibrary : TwoArgFunction() {
   override fun call(modname: LuaValue, env: LuaValue): LuaValue {
      env.set("sleep", SleepFunction())

      return LuaValue.NIL
   }

   class SleepFunction : OneArgFunction() {
      override fun call(ms: LuaValue): LuaValue {
         Thread.sleep(ms.checklong())
         return LuaValue.NIL
      }
   }
}