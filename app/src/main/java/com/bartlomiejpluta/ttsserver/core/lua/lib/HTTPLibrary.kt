package com.bartlomiejpluta.ttsserver.core.lua.lib

import com.bartlomiejpluta.ttsserver.core.web.mime.MimeType
import fi.iki.elonen.NanoHTTPD
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction

class HTTPLibrary : TwoArgFunction() {
   override fun call(modname: LuaValue, env: LuaValue): LuaValue {
      val methods = LuaValue.tableOf()
      val responses = LuaValue.tableOf()
      val mimeTypes = LuaValue.tableOf()
      NanoHTTPD.Method.values().forEach { methods.set(it.name, it.name) }
      NanoHTTPD.Response.Status.values().forEach { responses.set(it.name, it.requestStatus) }
      MimeType.values().forEach { mimeTypes.set(it.name, it.mimeType) }
      env.set("Method", methods)
      env.set("Status", responses)
      env.set("Mime", mimeTypes)
      return methods
   }

}