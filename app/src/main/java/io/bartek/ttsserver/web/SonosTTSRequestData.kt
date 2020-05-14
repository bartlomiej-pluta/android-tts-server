package io.bartek.ttsserver.web

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.ResponseException
import org.json.JSONObject
import java.util.*

data class SonosTTSRequestData(val text: String, val language: Locale, val zone: String, val volume: Int) {
   companion object {
      fun fromJSON(json: String): SonosTTSRequestData {
         val root = JSONObject(json)

         val language = root.optString("language")
            .takeIf { it.isNotBlank() }
            ?.let { Locale(it) }
            ?: Locale.US
         val text = root.optString("text") ?: throw ResponseException(
            NanoHTTPD.Response.Status.BAD_REQUEST,
            ""
         )
         val zone = root.optString("zone") ?: throw ResponseException(
            NanoHTTPD.Response.Status.BAD_REQUEST,
            ""
         )
         val volume = root.optInt("volume", 50)

         return SonosTTSRequestData(text, language, zone, volume)
      }
   }
}