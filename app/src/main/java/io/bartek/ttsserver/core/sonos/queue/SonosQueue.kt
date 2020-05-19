package io.bartek.ttsserver.core.sonos.queue

import io.bartek.ttsserver.core.sonos.worker.SonosWorker
import io.bartek.ttsserver.core.tts.engine.TTSEngine
import io.bartek.ttsserver.core.util.NetworkUtil
import io.bartek.ttsserver.core.web.dto.SonosDTO
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