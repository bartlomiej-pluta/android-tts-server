package com.bartlomiejpluta.ttsserver.core.web.endpoint

enum class Endpoint(val uri: String, val id: Int) {
   UNKNOWN("/", 1),
   SAY("/say", 2),
   WAVE("/wave", 3),
   AAC("/aac", 4),
   MP3("/mp3", 5),
   M4A("/m4a", 6),
   WMA("/wma", 7),
   FLAC("/flac", 8),
   SONOS("/sonos", 9),
   SONOS_CACHE("/sonos/*", 10),
   GONG("/gong.wav", 11);

   val trimmedUri: String
      get() = uri.replace("*", "")

   companion object {
      fun of(ordinal: Int) = values().firstOrNull { it.ordinal == ordinal } ?: UNKNOWN
   }
}