package io.bartek.ttsserver.web.dto

import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.ResponseException
import org.json.JSONObject

abstract class DTO {
   companion object {
      fun JSONObject.requiredString(key: String) = this.nullableString(key)
         ?: throw ResponseException(Response.Status.BAD_REQUEST, "")


      fun JSONObject.nullableString(key: String) = this.optString(key)
         .takeIf { it.isNotBlank() }
   }
}