package com.bartlomiejpluta.ttsserver.core.web.endpoint

import com.bartlomiejpluta.ttsserver.core.web.dto.Request
import com.bartlomiejpluta.ttsserver.core.web.uri.UriTemplate
import fi.iki.elonen.NanoHTTPD.*
import org.luaj.vm2.*
import org.luaj.vm2.lib.jse.CoerceLuaToJava
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

class DefaultEndpoint(
   uri: UriTemplate,
   accepts: String?,
   method: Method,
   private val consumer: LuaClosure
) : AbstractEndpoint(uri, accepts, method) {

   override fun safeHit(request: Request) = request.luaTable
      .let { consumer.call(it) }
      .checktable()
      .let { parseResponse(it) }

   private fun parseResponse(response: LuaValue) = response.checktable()
      .let { provideResponse(it) }


   private fun provideResponse(response: LuaTable) = when (response.get("data")) {
      is LuaString -> getTextResponse(response)
      is LuaUserdata -> getFileResponse(response)
      else -> throw IllegalArgumentException("Supported only string and file data types")
   }

   private fun getTextResponse(response: LuaTable) = newFixedLengthResponse(
      getStatus(response),
      getMimeType(response),
      getData(response)
   )

   private fun getFileResponse(response: LuaTable): Response? {
      val file = CoerceLuaToJava.coerce(response.get("data"), File::class.java) as File
      val stream = BufferedInputStream(FileInputStream(file))
      val length = file.length()

      if(!getCached(response)) {
         file.delete()
      }

      return newFixedLengthResponse(
         getStatus(response),
         getMimeType(response),
         stream,
         length
      )
   }

   private fun getStatus(response: LuaTable): Response.Status {
      val status = response.get("status").optint(Response.Status.OK.requestStatus)
      return Response.Status
         .values()
         .firstOrNull { it.requestStatus == status }
         ?: throw LuaError("Unsupported status: $status")
   }

   private fun getMimeType(response: LuaTable) = response.get("mime").optjstring("text/plain")

   private fun getData(response: LuaTable) = response.get("data").checkjstring()

   private fun getCached(response: LuaTable) = response.get("cached").optboolean(false)

   override fun toString() = "D[${uri.template}]"
}