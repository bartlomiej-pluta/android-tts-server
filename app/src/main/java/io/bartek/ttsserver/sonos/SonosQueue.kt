package io.bartek.ttsserver.sonos

import com.vmichalak.sonoscontroller.SonosDiscovery
import io.bartek.ttsserver.service.ForegroundService
import io.bartek.ttsserver.service.ServiceState
import io.bartek.ttsserver.tts.TTS
import io.bartek.ttsserver.web.SonosTTSRequestData
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

private class Consumer(
   private val tts: TTS,
   private val host: String,
   private val port: Int,
   private val queue: BlockingQueue<SonosTTSRequestData>
) : Runnable {

   override fun run() = try {
      while (ForegroundService.state == ServiceState.RUNNING) {
         consume(queue.take())
      }
   } catch (e: InterruptedException) {
      Thread.currentThread().interrupt()
   }


   private fun consume(data: SonosTTSRequestData) {
      SonosDiscovery.discover().firstOrNull { it.zoneGroupState.name == data.zone }?.let {
         val file = tts.createTTSFile(data.text, data.language)
         val filename = file.name
         val url = "http://$host:$port/sonos/$filename"
         val currentVolume = it.volume
         it.volume = data.volume
         it.clip(url, "")
         it.volume = currentVolume
      }
   }
}

class SonosQueue(tts: TTS, host: String, port: Int) {
   private val queue: BlockingQueue<SonosTTSRequestData> = LinkedBlockingQueue()
   private val consumer = Thread(Consumer(tts, host, port, queue)).also {
      it.name = "SONOS_QUEUE"
   }

   init { consumer.start() }

   fun push(data: SonosTTSRequestData) = queue.add(data)
}