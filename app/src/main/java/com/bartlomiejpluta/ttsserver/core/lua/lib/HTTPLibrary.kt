package com.bartlomiejpluta.ttsserver.core.lua.lib

import fi.iki.elonen.NanoHTTPD
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction

class HTTPLibrary : TwoArgFunction() {
   override fun call(modname: LuaValue, env: LuaValue): LuaValue {
      val methods = LuaValue.tableOf()
      val responses = LuaValue.tableOf()
      NanoHTTPD.Method.values().forEach { methods.set(it.name, it.name) }
      NanoHTTPD.Response.Status.values().forEach { responses.set(it.name, it.requestStatus) }
      env.set("Method", methods)
      env.set("Status", responses)
      return methods
   }

}