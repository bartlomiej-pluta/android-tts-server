package com.bartlomiejpluta.ttsserver.core.web.endpoint

import com.bartlomiejpluta.ttsserver.service.foreground.ForegroundService
import com.bartlomiejpluta.ttsserver.service.state.ServiceState
import org.luaj.vm2.LuaClosure
import java.util.concurrent.BlockingQueue

class Worker(
   private val queue: BlockingQueue<Request>,
   private val consumer: LuaClosure
) : Runnable {
   override fun run() = try {
      while (ForegroundService.state == ServiceState.RUNNING) {
         consume(queue.take())
      }
   } catch (e: InterruptedException) {
      Thread.currentThread().interrupt()
   }

   private fun consume(request: Request) {
      consumer.call(request.body, request.params)
   }
}