package com.bartlomiejpluta.ttsserver.core.web.queue

class TasksQueueFactory {
   fun create() = ThreadPoolBasedTasksQueue()
}