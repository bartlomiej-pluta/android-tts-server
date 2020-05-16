package io.bartek.ttsserver.web.dto

import org.json.JSONObject
import java.util.*

data class SonosDTO(val text: String, val language: Locale, val zone: String, val volume: Int) :
   DTO() {
   companion object {
      fun fromJSON(json: String) = JSONObject(json).let { root ->
         val language = root.nullableString("language") ?.let { Locale(it) } ?: Locale.US
         val text = root.requiredString("text")
         val zone = root.requiredString("zone")
         val volume = root.optInt("volume", 50)

         SonosDTO(text, language, zone, volume)
      }
   }
}