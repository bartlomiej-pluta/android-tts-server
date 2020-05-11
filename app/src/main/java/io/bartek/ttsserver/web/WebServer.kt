package io.bartek.ttsserver.web

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status.*
import io.bartek.ttsserver.preference.PreferenceKey
import io.bartek.ttsserver.service.ForegroundService
import io.bartek.ttsserver.service.ServiceState
import io.bartek.ttsserver.tts.TTS
import org.json.JSONObject
import java.util.*

private data class TTSRequestData(val text: String, val language: Locale)


class WebServer(port: Int, private val context: Context) : NanoHTTPD(port),
   TextToSpeech.OnInitListener {
   private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
   private val tts = TTS(context, this)

   override fun serve(session: IHTTPSession?): Response {
      try {
         session?.let {
            return when (it.uri) {
               "/wave" -> wave(it)
               "/say" -> say(it)
               else -> throw ResponseException(NOT_FOUND, "")
            }
         }

         throw ResponseException(BAD_REQUEST, "")
      } catch (e: ResponseException) {
         throw e
      } catch (e: Exception) {
         throw ResponseException(INTERNAL_ERROR, e.toString(), e)
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

      val (text, language) = getRequestData(session)
      val (stream, size) = tts.fetchTTSStream(text, language)
      return newFixedLengthResponse(OK, MIME_WAVE, stream, size)
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

      val (text, language) = getRequestData(session)
      tts.performTTS(text, language)
      return newFixedLengthResponse(OK, MIME_PLAINTEXT, "")
   }

   private fun getRequestData(session: IHTTPSession): TTSRequestData {
      val map = mutableMapOf<String, String>()
      session.parseBody(map)
      val json = JSONObject(map["postData"] ?: "{}")
      val language = json.optString("language")
         .takeIf { it.isNotBlank() }
         ?.let { Locale(it) }
         ?: Locale.US
      val text = json.optString("text") ?: throw ResponseException(BAD_REQUEST, "")
      return TTSRequestData(text, language)
   }

   override fun onInit(status: Int) = start()

   override fun start() {
      super.start()
      LocalBroadcastManager
         .getInstance(context)
         .sendBroadcast(Intent(ForegroundService.CHANGE_STATE).also {
            it.putExtra(ForegroundService.STATE, ServiceState.RUNNING.name)
         })
   }

   override fun stop() {
      super.stop()
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