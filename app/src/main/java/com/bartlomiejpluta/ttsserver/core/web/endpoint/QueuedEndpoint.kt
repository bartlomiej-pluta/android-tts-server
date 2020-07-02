package com.bartlomiejpluta.ttsserver.core.web.endpoint

import com.bartlomiejpluta.ttsserver.core.web.dto.Request
import com.bartlomiejpluta.ttsserver.core.web.uri.UriTemplate
import com.bartlomiejpluta.ttsserver.core.web.worker.Worker
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import org.luaj.vm2.LuaClosure
import java.util.concurrent.LinkedBlockingQueue

class QueuedEndpoint(
   private val uri: UriTemplate,
   private val accepts: String,
   private val method: NanoHTTPD.Method,
   consumer: LuaClosure
) : Endpoint {
   private val queue = LinkedBlockingQueue<Request>()
   private val worker = Thread(
      Worker(
         queue,
         consumer
      )
   ).also { it.name = uri.template }

   override fun hit(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
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

      queue.add(request)

      return newFixedLengthResponse(NanoHTTPD.Response.Status.ACCEPTED, "text/plain", "")
   }

   private fun extractBody(session: NanoHTTPD.IHTTPSession): String {
      return mutableMapOf<String, String>().let {
         session.parseBody(it)
         it["postData"] ?: ""
      }
   }

   fun runWorker() = worker.start()
   fun stopWorker() = worker.interrupt()

   override fun toString() = "Q[${uri.template}]"
}