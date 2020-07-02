package com.bartlomiejpluta.ttsserver.core.web.endpoint

import com.bartlomiejpluta.ttsserver.core.web.uri.UriTemplate
import fi.iki.elonen.NanoHTTPD
import org.luaj.vm2.LuaValue

class Request private constructor(
   request: NanoHTTPD.IHTTPSession,
   matchingResult: UriTemplate.MatchingResult
) {
   val luaTable = LuaValue.tableOf()

   init {
      val body = extractBody(request)
      val pathParams = extractPathParams(matchingResult)

      luaTable.set("body", body)
      luaTable.set("params", pathParams)
   }


   private fun extractPathParams(matchingResult: UriTemplate.MatchingResult) =
      LuaValue.tableOf().also { params ->
         matchingResult.variables
            .map { LuaValue.valueOf(it.key) to LuaValue.valueOf(it.value) }
            .forEach { params.set(it.first, it.second) }

      }

   private fun extractBody(request: NanoHTTPD.IHTTPSession) =
      mutableMapOf<String, String>().let {
         request.parseBody(it)
         it["postData"] ?: ""
      }

   companion object {
      fun of(request: NanoHTTPD.IHTTPSession, matchingResult: UriTemplate.MatchingResult) =
         Request(request, matchingResult)
   }
}