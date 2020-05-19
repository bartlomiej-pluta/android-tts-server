package com.bartlomiejpluta.ttsserver.core.sonos.queue

import com.bartlomiejpluta.ttsserver.core.sonos.worker.SonosWorker
import com.bartlomiejpluta.ttsserver.core.tts.engine.TTSEngine
import com.bartlomiejpluta.ttsserver.core.util.NetworkUtil
import com.bartlomiejpluta.ttsserver.core.web.dto.SonosDTO
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class SonosQueue(private val tts: TTSEngine, private val networkUtil: NetworkUtil) {
   private val queue: BlockingQueue<SonosDTO> = LinkedBlockingQueue()
   private var consumer: Thread? = null

   fun run() {
      consumer?.interrupt()
      consumer = Thread(SonosWorker(tts, networkUtil.serverAddress, queue))
         .also { it.name = "SonosQueue" }
      consumer?.start()
   }

   fun stop() {
      consumer?.interrupt()
      consumer = null
   }

   fun push(data: SonosDTO) = queue.add(data)
}