package io.bartek.ttsserver.web.server

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status.*
import io.bartek.ttsserver.preference.PreferenceKey
import io.bartek.ttsserver.service.ForegroundService
import io.bartek.ttsserver.service.ServiceState
import io.bartek.ttsserver.sonos.SonosQueue
import io.bartek.ttsserver.tts.TTS
import io.bartek.ttsserver.tts.TTSStatus
import io.bartek.ttsserver.web.dto.BaseDTO
import io.bartek.ttsserver.web.dto.SonosDTO
import io.bartek.ttsserver.web.endpoint.Endpoint
import io.bartek.ttsserver.web.endpoint.EndpointMatcher
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream


class WebServer(
   port: Int,
   private val context: Context,
   private val preferences: SharedPreferences,
   private val tts: TTS,
   private val sonos: SonosQueue
) : NanoHTTPD(port) {
   override fun serve(session: IHTTPSession?): Response {
      try {
         assertThatTTSIsReady()

         session?.let {
            return dispatch(it)
         }

         throw ResponseException(BAD_REQUEST, "")
      }
      catch (e: ResponseException) { throw e }
      catch (e: Exception) { throw ResponseException(INTERNAL_ERROR, e.toString(), e) }
   }

   private fun dispatch(it: IHTTPSession): Response {
      return when (EndpointMatcher.match(it.uri)) {
         Endpoint.SAY -> say(it)
         Endpoint.WAVE -> wave(it)
         Endpoint.SONOS -> sonos(it)
         Endpoint.SONOS_CACHE -> sonosCache(it)
         Endpoint.UNKNOWN -> throw ResponseException(NOT_FOUND, "")
      }
   }

   private fun assertThatTTSIsReady() {
      if (tts.status != TTSStatus.READY) {
         throw ResponseException(NOT_ACCEPTABLE, "Server is not ready yet")
      }
   }

   private fun say(session: IHTTPSession): Response {
      if (!preferences.getBoolean(PreferenceKey.ENABLE_SAY_ENDPOINT, true)) {
         throw ResponseException(NOT_FOUND, "")
      }

      if (session.method != Method.POST) {
         throw ResponseException(METHOD_NOT_ALLOWED, "")
      }

      if (session.headers[CONTENT_TYPE]?.let { it != MIME_JSON } != false) {
         throw ResponseException(BAD_REQUEST, "")
      }

      val (text, language) = extractBody(session) {
         SonosDTO.fromJSON(
            it
         )
      }

      tts.performTTS(text, language)
      return newFixedLengthResponse(OK, MIME_PLAINTEXT, "")
   }

   private fun <T> extractBody(session: IHTTPSession, provider: (String) -> T): T {
      return mutableMapOf<String, String>().let {
         session.parseBody(it)
         provider(it["postData"] ?: "{}")
      }
   }

   private fun wave(session: IHTTPSession): Response {
      if (!preferences.getBoolean(PreferenceKey.ENABLE_WAVE_ENDPOINT, true)) {
         throw ResponseException(NOT_FOUND, "")
      }

      if (session.method != Method.POST) {
         throw ResponseException(METHOD_NOT_ALLOWED, "")
      }

      if (session.headers[CONTENT_TYPE]?.let { it != MIME_JSON } != false) {
         throw ResponseException(BAD_REQUEST, "")
      }

      val (text, language) = extractBody(session) {
         SonosDTO.fromJSON(
            it
         )
      }

      val (stream, size) = tts.fetchTTSStream(text, language)
      return newFixedLengthResponse(OK,
         MIME_WAVE, stream, size)
   }

   private fun sonos(session: IHTTPSession): Response {
      if (!preferences.getBoolean(PreferenceKey.ENABLE_SONOS_ENDPOINT, true)) {
         throw ResponseException(NOT_FOUND, "")
      }

      if (session.method != Method.POST) {
         throw ResponseException(METHOD_NOT_ALLOWED, "")
      }

      if (session.headers[CONTENT_TYPE]?.let { it != MIME_JSON } != false) {
         throw ResponseException(BAD_REQUEST, "")
      }

      val data = extractBody(session) {
         BaseDTO.fromJSON(
            it
         )
      }

      sonos.push(data)

      return newFixedLengthResponse(ACCEPTED, MIME_PLAINTEXT, "")
   }

   private fun sonosCache(session: IHTTPSession): Response {
      if (!preferences.getBoolean(PreferenceKey.ENABLE_SONOS_ENDPOINT, true)) {
         throw ResponseException(NOT_FOUND, "")
      }

      if (session.method != Method.GET) {
         throw ResponseException(METHOD_NOT_ALLOWED, "")
      }

      val filename =
         Uri.parse(session.uri).lastPathSegment ?: throw ResponseException(BAD_REQUEST, "")
      val file = File(context.cacheDir, filename)

      if (!file.exists()) {
         throw ResponseException(NOT_FOUND, "")
      }

      val stream = BufferedInputStream(FileInputStream(file))
      val size = file.length()
      return newFixedLengthResponse(OK,
         MIME_WAVE, stream, size)
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
   }
}