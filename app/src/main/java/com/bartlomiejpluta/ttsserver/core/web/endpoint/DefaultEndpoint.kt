package com.bartlomiejpluta.ttsserver.core.web.endpoint

import com.bartlomiejpluta.ttsserver.core.web.uri.UriTemplate
import fi.iki.elonen.NanoHTTPD.*
import org.luaj.vm2.LuaClosure
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

class DefaultEndpoint(
   private val uri: UriTemplate,
   private val accepts: String,
   private val method: Method,
   private val consumer: LuaClosure
) : Endpoint {

   override fun hit(session: IHTTPSession): Response? {
      if (session.method != method) {
         return null
      }

      if ((session.headers["content-type"]?.let { it != accepts } != false)) {
         return null
      }

      val matchingResult = uri.match(session.uri)
      if (!matchingResult.matched) {
         return null
      }

      val request = Request.of(session, matchingResult)

      val response = consumer.call(request.luaTable).checktable()
      return parseResponse(response)
   }

   private fun parseResponse(response: LuaValue) = response
      .let {
         it as? LuaTable
            ?: throw IllegalArgumentException("Invalid type for response - expected table")
      }
      .let { provideResponse(it) }


   private fun provideResponse(response: LuaTable) =
      when (response.get("type").checkjstring()) {
         ResponseType.TEXT.name -> getTextResponse(response)
         ResponseType.FILE.name -> getFileResponse(response)
         else -> throw IllegalArgumentException("Unknown value for type in response")
      }

   private fun getTextResponse(response: LuaTable) = newFixedLengthResponse(
      getStatus(response),
      getMimeType(response),
      getData(response)
   )

   private fun getFileResponse(response: LuaTable): Response? {
      val file = File(response.get("file").checkstring().tojstring())
      val stream = BufferedInputStream(FileInputStream(file))
      val length = file.length()
      return newFixedLengthResponse(
         getStatus(response),
         getMimeType(response),
         stream,
         length
      )
   }

   private fun getStatus(response: LuaTable): Response.Status {
      val status = response.get("status").checkint()
      return Response.Status
         .values()
         .firstOrNull { it.requestStatus == status }
         ?: throw IllegalArgumentException("Unsupported status: $status")
   }

   private fun getMimeType(response: LuaTable) = response.get("mime").checkstring().tojstring()

   private fun getData(response: LuaTable) = response.get("data").checkstring().tojstring()

}