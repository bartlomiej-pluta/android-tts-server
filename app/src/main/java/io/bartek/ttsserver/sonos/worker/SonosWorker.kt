package io.bartek.ttsserver.sonos.worker

import com.vmichalak.sonoscontroller.SonosDevice
import com.vmichalak.sonoscontroller.SonosDiscovery
import com.vmichalak.sonoscontroller.model.PlayState
import io.bartek.ttsserver.service.ForegroundService
import io.bartek.ttsserver.service.ServiceState
import io.bartek.ttsserver.tts.engine.TTSEngine
import io.bartek.ttsserver.web.dto.SonosDTO
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
         it.announce(url, data.volume)
      }

   private fun SonosDevice.announce(url: String, volume: Int) {
      val currentPlayState = this.playState
      val currentVolume = this.volume

      this.stop()
      this.volume = volume
      this.clip(url, "")

      this.volume = currentVolume
      when(currentPlayState) {
         PlayState.PLAYING -> this.play()
         else -> this.stop()
      }
   }
}