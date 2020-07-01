package com.bartlomiejpluta.ttsserver.core.lua.lib

import com.bartlomiejpluta.ttsserver.core.web.endpoint.EndpointType
import fi.iki.elonen.NanoHTTPD
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction

class HTTPLibrary : TwoArgFunction() {
   override fun call(modname: LuaValue, env: LuaValue): LuaValue {
      val methods = LuaValue.tableOf()
      val responses = LuaValue.tableOf()
      val endpoints = LuaValue.tableOf()
      NanoHTTPD.Method.values().forEach { methods.set(it.name, it.name) }
      NanoHTTPD.Response.Status.values().forEach { responses.set(it.name, it.requestStatus) }
      EndpointType.values().forEach { endpoints.set(it.name, it.name) }
      env.set("Method", methods)
      env.set("Response", responses)
      env.set("Endpoint", endpoints)
      return methods
   }

}