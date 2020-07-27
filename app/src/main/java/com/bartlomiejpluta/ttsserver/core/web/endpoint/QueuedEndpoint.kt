package com.bartlomiejpluta.ttsserver.core.web.endpoint

import android.content.Context
import com.bartlomiejpluta.ttsserver.core.web.dto.Request
import com.bartlomiejpluta.ttsserver.core.web.queue.TasksQueue
import com.bartlomiejpluta.ttsserver.core.web.task.QueueableTask
import com.bartlomiejpluta.ttsserver.core.web.uri.UriTemplate
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import org.luaj.vm2.LuaClosure

class QueuedEndpoint(
   private val context: Context,
   private val queue: TasksQueue,
   private val consumer: LuaClosure,
   uri: UriTemplate,
   method: NanoHTTPD.Method,
   accepts: String?
) : AbstractEndpoint(uri, accepts, method) {

   override fun safeHit(request: Request): NanoHTTPD.Response? {
      val task = QueueableTask(context, consumer, request, queue)
      queue.push(task)
      return newFixedLengthResponse(NanoHTTPD.Response.Status.ACCEPTED, "text/plain", "")
   }

   fun shutdownQueue() = queue.shutdown()

   override fun toString() = "Q[${uri.template}]"
}