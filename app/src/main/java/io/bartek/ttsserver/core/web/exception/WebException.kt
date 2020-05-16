package io.bartek.ttsserver.core.web.exception

import fi.iki.elonen.NanoHTTPD.Response
import org.json.JSONObject


class WebException(val status: Response.Status, message: String? = null) : Exception(message) {
   val json: String
      get() = JSONObject().let { json ->
         json.put("status", status.requestStatus)
         json.put("description", status.description)
         message?.let { json.put("message", it) }
         json.toString()
      }
}