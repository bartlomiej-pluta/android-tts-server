package io.bartek.ttsserver.sonos

import com.vmichalak.sonoscontroller.SonosDiscovery
import java.io.File

class SonosController(private val host: String, private val port: Int) {
   fun clip(wave: File, zone: String, volume: Int) {
      SonosDiscovery.discover().firstOrNull { it.zoneGroupState.name == zone }?.let {
         val filename = wave.name
         val url = "http://$host:$port/sonos/$filename"
         val currentVolume = it.volume
         it.volume = volume
         it.clip(url, "")
         it.volume = currentVolume
      }
   }
}