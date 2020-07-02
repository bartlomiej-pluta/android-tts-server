package com.bartlomiejpluta.ttsserver.core.web.endpoint

import org.luaj.vm2.LuaValue

class Request private constructor(rawBody: String, paramsMap: Map<String, String>) {
   val body = LuaValue.valueOf(rawBody)
   val params = LuaValue.tableOf().also { params ->
      paramsMap
         .map { LuaValue.valueOf(it.key) to LuaValue.valueOf(it.value) }
         .forEach { params.set(it.first, it.second) }
   }

   companion object {
      fun of(body: String, params: Map<String, String>) = Request(body, params)
   }
}