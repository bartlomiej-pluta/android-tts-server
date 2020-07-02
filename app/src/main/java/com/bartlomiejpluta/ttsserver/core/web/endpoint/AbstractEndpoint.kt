package com.bartlomiejpluta.ttsserver.core.web.endpoint

import com.bartlomiejpluta.ttsserver.core.web.dto.Request
import com.bartlomiejpluta.ttsserver.core.web.uri.UriTemplate
import fi.iki.elonen.NanoHTTPD

abstract class AbstractEndpoint(
   protected val uri: UriTemplate,
   protected val accepts: String?,
   protected val method: NanoHTTPD.Method
) : Endpoint {
   override fun hit(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
      if (session.method != method) {
         return null
      }

      if (accepts?.let { it != session.headers["content-type"] } == true) {
         return null
      }

      val matchingResult = uri.match(session.uri)
      if (!matchingResult.matched) {
         return null
      }

      return safeHit(Request.of(session, matchingResult))
   }

   abstract fun safeHit(request: Request): NanoHTTPD.Response?
}