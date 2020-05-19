package com.bartlomiejpluta.ttsserver.core.web.dto

import com.bartlomiejpluta.ttsserver.core.web.exception.WebException
import fi.iki.elonen.NanoHTTPD.Response
import org.json.JSONObject

abstract class DTO(json: String) : JSONObject(json) {
   protected fun requiredString(key: String) = this.optString(key)
      .takeIf { it.isNotBlank() }
      ?: throw WebException(Response.Status.BAD_REQUEST, "The '$key' field is required")

   protected fun nullableInt(key: String, default: Int) = this.optInt(key, default)
      .also { put(key, it) }

   protected fun <T> nullableObject(
      key: String,
      default: T,
      deserializer: (String) -> T,
      serializer: (T) -> String
   ): T = this.optString(key)
      .takeIf { it.isNotBlank() }
      ?.let { deserializer(it) }
      ?: default.also { put(key, serializer(it)) }
}