package com.bartlomiejpluta.ttsserver.core.sonos.worker

import com.bartlomiejpluta.ttsserver.core.tts.engine.TTSEngine
import com.bartlomiejpluta.ttsserver.core.web.dto.SonosDTO
import com.bartlomiejpluta.ttsserver.service.foreground.ForegroundService
import com.bartlomiejpluta.ttsserver.service.state.ServiceState
import com.vmichalak.sonoscontroller.SonosDiscovery
import java.util.concurrent.BlockingQueue

class SonosWorker(
   private val tts: TTSEngine,
   private val address: String,
   private val queue: BlockingQueue<SonosDTO>
) : Runnable {

   override fun run() = try {
      while (ForegroundService.state == ServiceState.RUNNING) {
         consume(queue.take())
      }
   } catch (e: InterruptedException) {
      Thread.currentThread().interrupt()
   }

   private fun consume(data: SonosDTO) =
      SonosDiscovery.discover().firstOrNull { it.zoneGroupState.name == data.zone }?.let {
         val file = tts.createTTSFile(data.text, data.language)
         val filename = file.name
         val url = "$address/sonos/$filename"
         it.clip(url, data.volume, "")
      }
}