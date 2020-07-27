package com.bartlomiejpluta.ttsserver.core.web.queue

import com.bartlomiejpluta.ttsserver.core.web.task.QueueableTask
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ThreadPoolBasedTasksQueue : TasksQueue {
   private val executor = ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue())

   override fun push(queueableTask: QueueableTask) {
      executor.execute(queueableTask)
   }

   override fun shutdown() {
      executor.shutdown()
      executor.awaitTermination(5L, TimeUnit.SECONDS)
   }

   override val size: Int
      get() = executor.queue.size
}