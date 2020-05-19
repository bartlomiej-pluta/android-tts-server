package com.bartlomiejpluta.ttsserver.core.web.dto

import java.util.*

class BaseDTO(json: String) : DTO(json) {
   val language = nullableObject("language", Locale.US, { Locale(it) }, { it.toString() })
   val text = requiredString("text")
}