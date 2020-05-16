package io.bartek.ttsserver.web.dto

import org.json.JSONObject
import java.util.*

data class BaseDTO(val text: String, val language: Locale) : DTO() {
   companion object {
      fun fromJSON(json: String) = JSONObject(json).let {root ->
         val language = root.nullableString("language") ?.let { Locale(it) } ?: Locale.US
         val text = root.requiredString("text")

         BaseDTO(text, language)
      }
   }
}