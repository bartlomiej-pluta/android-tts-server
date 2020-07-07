package com.bartlomiejpluta.ttsserver.core.web.dto

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
      val pathParameters = extractPathParameters(matchingResult)
      val queryVariables = extractQueryVariables(request)

      luaTable.set("body", body)
      luaTable.set("query", queryVariables)
      luaTable.set("path", pathParameters)
   }


   private fun extractPathParameters(matchingResult: UriTemplate.MatchingResult) =
      LuaValue.tableOf().also { table ->
         matchingResult.variables
            .map { LuaValue.valueOf(it.key) to LuaValue.valueOf(it.value) }
            .forEach { table.set(it.first, it.second) }

      }

   private fun extractQueryVariables(request: NanoHTTPD.IHTTPSession) =
      LuaValue.tableOf().also { table ->
         request.parms
            .map { LuaValue.valueOf(it.key) to LuaValue.valueOf(it.value) }
            .forEach { table.set(it.first, it.second) }
      }

   private fun extractBody(request: NanoHTTPD.IHTTPSession) =
      mutableMapOf<String, String>().let {
         request.parseBody(it)
         it["postData"] ?: ""
      }

   companion object {
      fun of(request: NanoHTTPD.IHTTPSession, matchingResult: UriTemplate.MatchingResult) =
         Request(
            request,
            matchingResult
         )
   }
}