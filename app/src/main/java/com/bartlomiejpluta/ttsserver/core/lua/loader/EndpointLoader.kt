package com.bartlomiejpluta.ttsserver.core.lua.loader

import android.content.Context
import com.bartlomiejpluta.ttsserver.core.lua.sandbox.SandboxFactory
import com.bartlomiejpluta.ttsserver.core.web.endpoint.DefaultEndpoint
import com.bartlomiejpluta.ttsserver.core.web.endpoint.Endpoint
import com.bartlomiejpluta.ttsserver.core.web.endpoint.QueuedEndpoint
import com.bartlomiejpluta.ttsserver.core.web.uri.UriTemplate
import fi.iki.elonen.NanoHTTPD.Method
import org.luaj.vm2.LuaClosure
import org.luaj.vm2.LuaNil
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaTable

class EndpointLoader(
   private val context: Context,
   private val sandboxFactory: SandboxFactory
) {

   fun loadEndpoints(): List<Endpoint> {
      val scripts = context.getExternalFilesDir("Endpoints")?.listFiles() ?: emptyArray()

      return scripts
         .map { sandboxFactory.createSandbox().loadfile(it.absolutePath).call() }
         .map {
            it as? LuaTable
               ?: throw IllegalArgumentException("Expected single table to be returned")
         }
         .filter { parseEnabled(it) }
         .map { createEndpoint(it) }
   }

   private fun createEndpoint(luaTable: LuaTable) = when (parseQueued(luaTable)) {
      false -> createDefaultEndpoint(luaTable)
      true -> createQueuedEndpoint(luaTable)
   }

   private fun createDefaultEndpoint(luaTable: LuaTable): Endpoint = DefaultEndpoint(
      uri = parseUri(luaTable),
      method = parseMethod(luaTable),
      accepts = parseAccepts(luaTable),
      consumer = parseConsumer(luaTable)
   )

   private fun createQueuedEndpoint(luaTable: LuaTable): Endpoint = QueuedEndpoint(
      uri = parseUri(luaTable),
      method = parseMethod(luaTable),
      accepts = parseAccepts(luaTable),
      consumer = parseConsumer(luaTable)
   )

   private fun parseUri(luaTable: LuaTable) = luaTable.get("uri")
      .takeIf { it !is LuaNil }
      ?.let { it as? LuaString ?: throw IllegalArgumentException("'uri' must be of string type'") }
      ?.tojstring()
      ?.let { UriTemplate.parse(it) }
      ?: throw IllegalArgumentException("'uri' field is required")

   private fun parseConsumer(luaTable: LuaTable) = luaTable.get("consumer")
      .takeIf { it !is LuaNil }
      ?.let {
         it as? LuaClosure ?: throw IllegalArgumentException("'consumer' must be a function'")
      }
      ?: throw IllegalArgumentException("'consumer' field is required")

   private fun parseEnabled(luaTable: LuaTable) = luaTable.get("enabled")
      .takeIf { it !is LuaNil }
      ?.checkboolean()
      ?: true

   private fun parseAccepts(luaTable: LuaTable) = luaTable.get("accepts")
      .takeIf { it !is LuaNil }
      ?.let {
         it as? LuaString ?: throw IllegalArgumentException("'accepts' must be of string type'")
      }
      ?.tojstring()
      ?: "text/plain"

   private fun parseQueued(luaTable: LuaTable) = luaTable.get("queued")
      .takeIf { it !is LuaNil }
      ?.checkboolean()
      ?: false

   private fun parseMethod(luaTable: LuaTable) = luaTable.get("method")
      .takeIf { it !is LuaNil }
      ?.let {
         it as? LuaString ?: throw IllegalArgumentException("'method' must be of string type'")
      }
      ?.let { Method.valueOf(it.tojstring()) }
      ?: Method.GET
}