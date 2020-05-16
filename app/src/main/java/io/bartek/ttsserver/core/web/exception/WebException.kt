package io.bartek.ttsserver.core.web.exception

import fi.iki.elonen.NanoHTTPD.Response
import org.json.JSONObject


class WebException(val status: Response.Status, message: String? = null) : Exception(message) {
   val json: String
      get() = message?.takeIf { it.isNotBlank() }
         ?.let { JSONObject().put("message", it).toString() }
         ?: ""
}