package com.bartlomiejpluta.ttsserver.core.web.endpoint

enum class Endpoint(val uri: String, val id: Int) {
   UNKNOWN("/", 1),
   SAY("/say", 2),
   WAVE("/wave", 3),
   SONOS("/sonos", 4),
   SONOS_CACHE("/sonos/*", 5);

   companion object {
      fun of(id: Int) = values().firstOrNull { it.id == id } ?: UNKNOWN
   }
}