package com.bartlomiejpluta.ttsserver.core.web.queue

import com.bartlomiejpluta.ttsserver.core.web.task.QueueableTask

interface TasksQueue {
   fun push(queueableTask: QueueableTask)
   fun shutdown()
   val size: Int
}