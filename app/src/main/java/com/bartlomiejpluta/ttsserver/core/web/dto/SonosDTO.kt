package com.bartlomiejpluta.ttsserver.core.web.dto

import java.util.*

class SonosDTO(json: String) : DTO(json) {
   val language = nullableObject("language", Locale.US, { Locale(it) }, { it.toString() })
   val text = requiredString("text")
   val zone = requiredString("zone")
   val volume = nullableInt("volume", 50)
}