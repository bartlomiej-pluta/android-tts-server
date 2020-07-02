package com.bartlomiejpluta.ttsserver.core.lua.loader

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bartlomiejpluta.ttsserver.core.lua.sandbox.SandboxFactory
import com.bartlomiejpluta.ttsserver.core.web.endpoint.DefaultEndpoint
import com.bartlomiejpluta.ttsserver.core.web.endpoint.Endpoint
import com.bartlomiejpluta.ttsserver.core.web.endpoint.QueuedEndpoint
import com.bartlomiejpluta.ttsserver.core.web.uri.UriTemplate
import com.bartlomiejpluta.ttsserver.ui.main.MainActivity
import fi.iki.elonen.NanoHTTPD.Method
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaTable
import java.io.File

class EndpointLoader(
   private val context: Context,
   private val sandboxFactory: SandboxFactory
) {

   fun loadEndpoints(): List<Endpoint> {
      val scripts = context.getExternalFilesDir("Endpoints")?.listFiles() ?: emptyArray()

      return scripts.mapNotNull { loadEndpoint(it) }
   }

   private fun loadEndpoint(script: File): Endpoint? {
      try {
         return sandboxFactory
            .createSandbox()
            .loadfile(script.absolutePath)
            .call()
            .checktable()
            .takeIf { parseEnabled(it) }
            ?.let { createEndpoint(it) }
      } catch (e: LuaError) {
         handleError(e)
         return null
      } catch (e: Exception) {
         throw e
      }
   }

   private fun handleError(exception: LuaError) = LocalBroadcastManager
      .getInstance(context)
      .sendBroadcast(Intent(MainActivity.LUA_ERROR).also {
         it.putExtra(MainActivity.MESSAGE, exception.message)
      })


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

   private fun parseUri(luaTable: LuaTable) = luaTable.get("uri").checkjstring()
      .let { UriTemplate.parse(it) }

   private fun parseConsumer(luaTable: LuaTable) = luaTable.get("consumer").checkclosure()

   private fun parseEnabled(luaTable: LuaTable) = luaTable.get("enabled").optboolean(true)

   private fun parseAccepts(luaTable: LuaTable) = luaTable.get("accepts").optjstring("")
      .takeIf { it.isNotBlank() }

   private fun parseQueued(luaTable: LuaTable) = luaTable.get("queued").optboolean(false)

   private fun parseMethod(luaTable: LuaTable) = luaTable.get("method").optjstring(Method.GET.name)
      .let { method -> Method.values().firstOrNull { it.name == method } }
      ?: throw LuaError("Invalid HTTP method. Allowed methods are: $ALLOWED_METHODS")

   companion object {
      private val ALLOWED_METHODS = Method.values().joinToString(", ")
   }
}