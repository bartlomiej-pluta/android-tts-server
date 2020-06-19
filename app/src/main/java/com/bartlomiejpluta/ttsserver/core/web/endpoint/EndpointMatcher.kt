package com.bartlomiejpluta.ttsserver.core.web.endpoint

import android.content.UriMatcher
import android.net.Uri

object EndpointMatcher {
   private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

   init {
      Endpoint.values().forEach {
         uriMatcher.addURI("", it.uri, it.ordinal)
      }
   }

   fun match(uri: String) =
      Endpoint.of(uriMatcher.match(Uri.parse("content://$uri")))
}