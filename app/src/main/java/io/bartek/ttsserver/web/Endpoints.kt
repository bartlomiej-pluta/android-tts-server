package io.bartek.ttsserver.web

import android.content.UriMatcher
import android.net.Uri

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

object Endpoints {
   private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

   init {
      Endpoint.values().forEach {
         uriMatcher.addURI("", it.uri, it.id)
      }
   }

   fun match(uri: String) =
      Endpoint.of(uriMatcher.match(Uri.parse("content://$uri")))
}