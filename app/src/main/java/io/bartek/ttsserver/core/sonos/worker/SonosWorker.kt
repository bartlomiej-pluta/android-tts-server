package io.bartek.ttsserver.core.sonos.worker

import com.vmichalak.sonoscontroller.SonosDiscovery
import io.bartek.ttsserver.core.tts.engine.TTSEngine
import io.bartek.ttsserver.core.web.dto.SonosDTO
import io.bartek.ttsserver.service.foreground.ForegroundService
import io.bartek.ttsserver.service.state.ServiceState
import java.util.concurrent.BlockingQueue

class SonosWorker(
   private val tts: TTSEngine,
   private val host: String,
   private val port: Int,
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
         val url = "http://$host:$port/sonos/$filename"
         it.clip(url, data.volume, "")
      }
}