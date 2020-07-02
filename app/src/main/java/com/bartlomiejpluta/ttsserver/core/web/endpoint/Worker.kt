package com.bartlomiejpluta.ttsserver.core.web.endpoint

import com.bartlomiejpluta.ttsserver.service.foreground.ForegroundService
import com.bartlomiejpluta.ttsserver.service.state.ServiceState
import org.luaj.vm2.LuaClosure
import org.luaj.vm2.LuaInteger
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.ZeroArgFunction
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
      consumer.call(request.luaTable, QueueSizeFunction(queue))
   }

   class QueueSizeFunction(private val queue: BlockingQueue<Request>) : ZeroArgFunction() {
      override fun call(): LuaInteger = LuaValue.valueOf(queue.size)
   }
}