package com.bartlomiejpluta.ttsserver.core.sonos.worker

import com.bartlomiejpluta.ttsserver.core.tts.engine.TTSEngine
import com.bartlomiejpluta.ttsserver.core.web.dto.SonosDTO
import com.bartlomiejpluta.ttsserver.service.foreground.ForegroundService
import com.bartlomiejpluta.ttsserver.service.state.ServiceState
import com.vmichalak.sonoscontroller.Snapshot
import com.vmichalak.sonoscontroller.SonosDevice
import com.vmichalak.sonoscontroller.SonosDiscovery
import com.vmichalak.sonoscontroller.model.PlayState
import java.util.concurrent.BlockingQueue

class SonosWorker(
   private val tts: TTSEngine,
   private val address: String,
   private val queue: BlockingQueue<SonosDTO>
) : Runnable {
   private var snapshot: Snapshot? = null

   override fun run() = try {
      while (ForegroundService.state == ServiceState.RUNNING) {
         consume(queue.take())
      }
   } catch (e: InterruptedException) {
      Thread.currentThread().interrupt()
   }

   private fun consume(data: SonosDTO) =
      SonosDiscovery.discover().firstOrNull { it.zoneGroupState.name == data.zone }?.let {
         updateSnapshotIfFirst(it)
         val url = prepareTTSFile(data)
         announce(it, data, url)
         restoreSnapshotIfLast()
      }

   private fun prepareTTSFile(data: SonosDTO): String {
      val filename = tts.createTTSFile(data.text, data.language).name
      return "$address/sonos/$filename"
   }

   private fun announce(device: SonosDevice, data: SonosDTO, url: String) {
      device.stop()
      device.volume = data.volume
      device.playUri(url, "")
      while (device.playState != PlayState.STOPPED) {
         Thread.sleep(500)
      }
   }

   private fun updateSnapshotIfFirst(it: SonosDevice) {
      if (snapshot == null) {
         snapshot = it.snapshot()
      }
   }

   private fun restoreSnapshotIfLast() {
      if (queue.isEmpty()) {
         snapshot!!.restore()
         snapshot = null
      }
   }
}