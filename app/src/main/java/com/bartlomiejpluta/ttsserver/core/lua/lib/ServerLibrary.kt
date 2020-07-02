package com.bartlomiejpluta.ttsserver.core.lua.lib

import com.bartlomiejpluta.ttsserver.core.util.NetworkUtil
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction

class ServerLibrary(private val networkUtil: NetworkUtil) : TwoArgFunction() {
   override fun call(modname: LuaValue, env: LuaValue): LuaValue {
      val server = LuaValue.tableOf().apply {
         set("port", networkUtil.port)
         set("address", networkUtil.address)
         set("url", networkUtil.url)
      }

      env.set("server", server)

      return LuaValue.NIL
   }
}