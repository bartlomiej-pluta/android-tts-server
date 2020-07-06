package com.bartlomiejpluta.ttsserver.core.web.worker

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bartlomiejpluta.ttsserver.core.web.dto.Request
import com.bartlomiejpluta.ttsserver.service.foreground.ForegroundService
import com.bartlomiejpluta.ttsserver.service.state.ServiceState
import com.bartlomiejpluta.ttsserver.ui.main.MainActivity
import org.luaj.vm2.LuaClosure
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaInteger
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.ZeroArgFunction
import java.util.concurrent.BlockingQueue

class Worker(
   private val context: Context,
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

   private fun consume(request: Request) = try {
      consumer.call(request.luaTable, QueueSizeFunction(queue))
   } catch (e: LuaError) {
      handleLuaError(e)
   }

   private fun handleLuaError(exception: LuaError) {
      LocalBroadcastManager
         .getInstance(context)
         .sendBroadcast(Intent(MainActivity.LUA_ERROR).also {
            it.putExtra(MainActivity.MESSAGE, exception.message)
         })
   }

   class QueueSizeFunction(private val queue: BlockingQueue<Request>) : ZeroArgFunction() {
      override fun call(): LuaInteger = LuaValue.valueOf(queue.size)
   }
}