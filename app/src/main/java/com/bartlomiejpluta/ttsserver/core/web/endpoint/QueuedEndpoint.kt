package com.bartlomiejpluta.ttsserver.core.web.endpoint

import android.content.Context
import com.bartlomiejpluta.ttsserver.core.web.dto.Request
import com.bartlomiejpluta.ttsserver.core.web.uri.UriTemplate
import com.bartlomiejpluta.ttsserver.core.web.worker.Worker
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import org.luaj.vm2.LuaClosure
import java.util.concurrent.LinkedBlockingQueue

class QueuedEndpoint(
   context: Context,
   uri: UriTemplate,
   accepts: String?,
   method: NanoHTTPD.Method,
   consumer: LuaClosure
) : AbstractEndpoint(uri, accepts, method) {
   private val queue = LinkedBlockingQueue<Request>()
   private val worker = Thread(Worker(context, queue, consumer)).also { it.name = uri.template }


   override fun safeHit(request: Request): NanoHTTPD.Response? {
      queue.add(request)
      return newFixedLengthResponse(NanoHTTPD.Response.Status.ACCEPTED, "text/plain", "")
   }

   fun runWorker() = worker.start()
   fun stopWorker() = worker.interrupt()

   override fun toString() = "Q[${uri.template}]"
}