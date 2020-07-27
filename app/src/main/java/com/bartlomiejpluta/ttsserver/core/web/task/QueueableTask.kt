package com.bartlomiejpluta.ttsserver.core.web.task

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bartlomiejpluta.R
import com.bartlomiejpluta.ttsserver.core.web.dto.Request
import com.bartlomiejpluta.ttsserver.core.web.queue.TasksQueue
import com.bartlomiejpluta.ttsserver.ui.main.MainActivity
import org.luaj.vm2.LuaClosure
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaInteger
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.ZeroArgFunction

class QueueableTask(
   private val context: Context,
   private val consumer: LuaClosure,
   private val request: Request,
   private val queue: TasksQueue
) : Runnable {

   override fun run() {
      try {
         consumer.call(request.luaTable, QueueSizeFunction(queue))
      } catch (e: LuaError) {
         handleLuaError(e)
      }
   }


   private fun handleLuaError(exception: LuaError) {
      LocalBroadcastManager
         .getInstance(context)
         .sendBroadcast(Intent(MainActivity.POPUP).apply {
            putExtra(MainActivity.TITLE, context.resources.getString(R.string.error))
            putExtra(MainActivity.MESSAGE, exception.message)
         })
   }

   class QueueSizeFunction(private val queue: TasksQueue) : ZeroArgFunction() {
      override fun call(): LuaInteger = LuaValue.valueOf(queue.size)
   }
}