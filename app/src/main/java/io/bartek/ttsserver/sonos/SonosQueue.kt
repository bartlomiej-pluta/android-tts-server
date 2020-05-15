package io.bartek.ttsserver.sonos

import android.content.SharedPreferences
import com.vmichalak.sonoscontroller.SonosDiscovery
import io.bartek.ttsserver.preference.PreferenceKey
import io.bartek.ttsserver.service.ForegroundService
import io.bartek.ttsserver.service.ServiceState
import io.bartek.ttsserver.tts.engine.TTSEngine
import io.bartek.ttsserver.util.NetworkUtil
import io.bartek.ttsserver.web.dto.BaseDTO
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

private class Consumer(
   private val tts: TTSEngine,
   private val host: String,
   private val port: Int,
   private val queue: BlockingQueue<BaseDTO>
) : Runnable {

   override fun run() = try {
      while (ForegroundService.state == ServiceState.RUNNING) {
         consume(queue.take())
      }
   } catch (e: InterruptedException) {
      Thread.currentThread().interrupt()
   }


   private fun consume(data: BaseDTO) =
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

class SonosQueue(
   private val tts: TTSEngine,
   private val networkUtil: NetworkUtil,
   private val preferences: SharedPreferences
) {
   private val queue: BlockingQueue<BaseDTO> = LinkedBlockingQueue()
   private val host: String
      get() = networkUtil.getIpAddress()
   private val port: Int
      get() = preferences.getInt(PreferenceKey.PORT, 8080)
   private var consumer: Thread? = null

   fun run() {
      consumer?.interrupt()
      consumer = Thread(Consumer(tts, host, port, queue)).also { it.name = "SonosQueue" }
      consumer?.start()
   }

   fun stop() {
      consumer?.interrupt()
      consumer = null
   }

   fun push(data: BaseDTO) = queue.add(data)
}