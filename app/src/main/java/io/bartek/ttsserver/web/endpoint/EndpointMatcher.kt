package io.bartek.ttsserver.web.endpoint

import android.content.UriMatcher
import android.net.Uri

object EndpointMatcher {
   private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

   init {
      Endpoint.values().forEach {
         uriMatcher.addURI("", it.uri, it.id)
      }
   }

   fun match(uri: String) =
      Endpoint.of(uriMatcher.match(Uri.parse("content://$uri")))
}