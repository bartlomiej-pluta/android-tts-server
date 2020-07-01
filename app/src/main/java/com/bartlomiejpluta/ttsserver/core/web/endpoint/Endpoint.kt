package com.bartlomiejpluta.ttsserver.core.web.endpoint

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoHTTPD.Response

interface Endpoint {
   fun hit(session: IHTTPSession): Response?
}