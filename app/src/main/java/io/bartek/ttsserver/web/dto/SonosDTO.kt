package io.bartek.ttsserver.web.dto

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.ResponseException
import org.json.JSONObject
import java.util.*

data class SonosDTO(val text: String, val language: Locale) {
   companion object {
      fun fromJSON(json: String): SonosDTO {
         val root = JSONObject(json)

         val language = root.optString("language")
            .takeIf { it.isNotBlank() }
            ?.let { Locale(it) }
            ?: Locale.US
         val text = root.optString("text") ?: throw ResponseException(
            NanoHTTPD.Response.Status.BAD_REQUEST,
            ""
         )

         return SonosDTO(text, language)
      }
   }
}