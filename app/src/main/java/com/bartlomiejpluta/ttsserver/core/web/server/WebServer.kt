package com.bartlomiejpluta.ttsserver.core.web.server

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bartlomiejpluta.ttsserver.core.sonos.queue.SonosQueue
import com.bartlomiejpluta.ttsserver.core.tts.engine.TTSEngine
import com.bartlomiejpluta.ttsserver.core.tts.exception.TTSException
import com.bartlomiejpluta.ttsserver.core.tts.status.TTSStatus
import com.bartlomiejpluta.ttsserver.core.web.dto.BaseDTO
import com.bartlomiejpluta.ttsserver.core.web.dto.SonosDTO
import com.bartlomiejpluta.ttsserver.core.web.endpoint.Endpoint
import com.bartlomiejpluta.ttsserver.core.web.endpoint.EndpointMatcher
import com.bartlomiejpluta.ttsserver.core.web.exception.WebException
import com.bartlomiejpluta.ttsserver.service.foreground.ForegroundService
import com.bartlomiejpluta.ttsserver.service.state.ServiceState
import com.bartlomiejpluta.ttsserver.ui.preference.PreferenceKey
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status.*
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream


class WebServer(
   port: Int,
   private val context: Context,
   private val preferences: SharedPreferences,
   private val tts: TTSEngine,
   private val sonos: SonosQueue
) : NanoHTTPD(port) {
   override fun serve(session: IHTTPSession?): Response {
      try {
         assertThatTTSIsReady()

         session?.let {
            return dispatch(it)
         }

         throw WebException(BAD_REQUEST, "Unknown error")
      } catch (e: WebException) {
         return handleWebException(e)
      } catch (e: Exception) {
         return handleUnknownException(e)
      }
   }

   private fun assertThatTTSIsReady() {
      if (tts.status != TTSStatus.READY) {
         throw WebException(NOT_ACCEPTABLE, "Server is not ready yet")
      }
   }

   private fun dispatch(it: IHTTPSession): Response {
      return when (EndpointMatcher.match(it.uri)) {
         Endpoint.SAY -> say(it)
         Endpoint.WAVE -> wave(it)
         Endpoint.SONOS -> sonos(it)
         Endpoint.SONOS_CACHE -> sonosCache(it)
         Endpoint.GONG -> gong(it)
         Endpoint.UNKNOWN -> throw WebException(NOT_FOUND)
      }
   }

   private fun handleWebException(e: WebException) =
      newFixedLengthResponse(e.status, MIME_JSON, e.json)

   private fun handleUnknownException(e: Exception): Response {
      val stacktrace = when (preferences.getBoolean(PreferenceKey.ENABLE_HTTP_DEBUG, false)) {
         true -> e.toString()
         false -> ""
      }

      return newFixedLengthResponse(INTERNAL_ERROR, MIME_PLAINTEXT, stacktrace)
   }

   private fun say(session: IHTTPSession): Response {
      if (!preferences.getBoolean(PreferenceKey.ENABLE_SAY_ENDPOINT, true)) {
         throw WebException(NOT_FOUND)
      }

      if (session.method != Method.POST) {
         throw WebException(METHOD_NOT_ALLOWED, "Only POST methods are allowed")
      }

      if (session.headers[CONTENT_TYPE]?.let { it != MIME_JSON } != false) {
         throw WebException(BAD_REQUEST, "Only JSON data is accepted")
      }

      val dto = extractBody(session) { BaseDTO(it) }

      tts.performTTS(dto.text, dto.language)
      return newFixedLengthResponse(OK, MIME_JSON, SUCCESS_RESPONSE)
   }

   private fun <T> extractBody(session: IHTTPSession, provider: (String) -> T): T {
      return mutableMapOf<String, String>().let {
         session.parseBody(it)
         provider(it["postData"] ?: "{}")
      }
   }

   private fun wave(session: IHTTPSession): Response {
      if (!preferences.getBoolean(PreferenceKey.ENABLE_WAVE_ENDPOINT, true)) {
         throw WebException(NOT_FOUND)
      }

      if (session.method != Method.POST) {
         throw WebException(METHOD_NOT_ALLOWED, "Only POST methods are allowed")
      }

      if (session.headers[CONTENT_TYPE]?.let { it != MIME_JSON } != false) {
         throw WebException(BAD_REQUEST, "Only JSON data is accepted")
      }

      val dto = extractBody(session) { BaseDTO(it) }

      val (stream, size) = tts.fetchTTSStream(dto.text, dto.language)
      return newFixedLengthResponse(OK, MIME_WAVE, stream, size)
   }

   private fun sonos(session: IHTTPSession): Response {
      if (!preferences.getBoolean(PreferenceKey.ENABLE_SONOS_ENDPOINT, true)) {
         throw WebException(NOT_FOUND)
      }

      if (session.method != Method.POST) {
         throw WebException(METHOD_NOT_ALLOWED, "Only POST methods are allowed")
      }

      if (session.headers[CONTENT_TYPE]?.let { it != MIME_JSON } != false) {
         throw WebException(BAD_REQUEST, "Only JSON data is accepted")
      }

      val dto = extractBody(session) { SonosDTO(it) }

      sonos.push(dto)

      return newFixedLengthResponse(ACCEPTED, MIME_JSON, QUEUED_RESPONSE)
   }

   private fun sonosCache(session: IHTTPSession): Response {
      if (!preferences.getBoolean(PreferenceKey.ENABLE_SONOS_ENDPOINT, true)) {
         throw WebException(NOT_FOUND)
      }

      if (session.method != Method.GET) {
         throw WebException(METHOD_NOT_ALLOWED, "Only GET methods are allowed")
      }

      val filename = Uri.parse(session.uri).lastPathSegment ?: throw WebException(BAD_REQUEST)
      val file = File(context.cacheDir, filename)

      if (!file.exists()) {
         throw WebException(NOT_FOUND)
      }

      val stream = BufferedInputStream(FileInputStream(file))
      val size = file.length()
      return newFixedLengthResponse(OK, MIME_WAVE, stream, size)
   }

   private fun gong(session: IHTTPSession): Response {
      if (!preferences.getBoolean(PreferenceKey.ENABLE_GONG, false)) {
         throw WebException(NOT_FOUND)
      }

      if (session.method != Method.GET) {
         throw WebException(METHOD_NOT_ALLOWED, "Only GET methods are allowed")
      }

      val uri = Uri.parse(
         preferences.getString(PreferenceKey.GONG, null) ?: throw TTSException()
      )

      val size = context.contentResolver.openFileDescriptor(uri, "r")?.statSize
         ?: throw TTSException()

      val stream = BufferedInputStream(
         context.contentResolver.openInputStream(uri) ?: throw TTSException()
      )

      return newFixedLengthResponse(OK, MIME_WAVE, stream, size)
   }

   override fun start() {
      super.start()
      sonos.run()
      LocalBroadcastManager
         .getInstance(context)
         .sendBroadcast(Intent(ForegroundService.CHANGE_STATE).also {
            it.putExtra(ForegroundService.STATE, ServiceState.RUNNING.name)
         })
   }

   override fun stop() {
      super.stop()
      sonos.stop()
      LocalBroadcastManager
         .getInstance(context)
         .sendBroadcast(Intent(ForegroundService.CHANGE_STATE).also {
            it.putExtra(ForegroundService.STATE, ServiceState.STOPPED.name)
         })
   }

   companion object {
      private const val MIME_JSON = "application/json"
      private const val MIME_WAVE = "audio/x-wav"
      private const val CONTENT_TYPE = "content-type"
      private val SUCCESS_RESPONSE = response("Request has been completed")
      private val QUEUED_RESPONSE = response("Request has been queued")

      private fun response(status: String) = JSONObject().put("message", status).toString()
   }
}