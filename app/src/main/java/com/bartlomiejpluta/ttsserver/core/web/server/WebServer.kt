package com.bartlomiejpluta.ttsserver.core.web.server

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bartlomiejpluta.ttsserver.core.log.service.LogService
import com.bartlomiejpluta.ttsserver.core.tts.engine.TTSEngine
import com.bartlomiejpluta.ttsserver.core.tts.status.TTSStatus
import com.bartlomiejpluta.ttsserver.core.web.endpoint.Endpoint
import com.bartlomiejpluta.ttsserver.core.web.endpoint.QueuedEndpoint
import com.bartlomiejpluta.ttsserver.core.web.exception.WebException
import com.bartlomiejpluta.ttsserver.service.state.ServiceState
import com.bartlomiejpluta.ttsserver.ui.main.MainActivity
import com.bartlomiejpluta.ttsserver.ui.preference.key.PreferenceKey
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status.*
import org.luaj.vm2.LuaError


class WebServer(
   port: Int,
   private val context: Context,
   private val preferences: SharedPreferences,
   private val tts: TTSEngine,
   private val endpoints: List<Endpoint>,
   private val log: LogService
) : NanoHTTPD(port) {
   private val queuedEndpoints = endpoints.mapNotNull { it as? QueuedEndpoint }

   override fun serve(session: IHTTPSession?): Response {
      try {
         assertThatTTSIsReady()

         session?.let {
            return dispatch(it)
         }

         throw WebException(BAD_REQUEST, "Unknown error")
      } catch (e: WebException) {
         log.error(TAG, "Web exception occurred: ${e.message}")
         return handleWebException(e)
      } catch (e: LuaError) {
         log.error(TAG, "Lua error occurred: ${e.message}")
         return handleLuaError(e)
      } catch (e: Exception) {
         log.fatal(TAG, "Unknown error occurred: ${e.message}")
         return handleUnknownException(e)
      }
   }

   private fun assertThatTTSIsReady() {
      if (tts.status != TTSStatus.READY) {
         throw WebException(NOT_ACCEPTABLE, "Server is not ready yet")
      }
   }

   private fun dispatch(session: IHTTPSession): Response {
      for (endpoint in endpoints) {
         endpoint.hit(session)?.let { return it }
      }

      throw WebException(NOT_FOUND)
   }

   private fun handleWebException(e: WebException) =
      newFixedLengthResponse(e.status, MIME_JSON, e.json)

   private fun handleLuaError(e: LuaError) =
      newFixedLengthResponse(INTERNAL_ERROR, MIME_PLAINTEXT, e.message)

   private fun handleUnknownException(e: Exception): Response {
      val stacktrace = when (preferences.getBoolean(PreferenceKey.ENABLE_HTTP_DEBUG, false)) {
         true -> e.toString()
         false -> ""
      }

      return newFixedLengthResponse(INTERNAL_ERROR, MIME_PLAINTEXT, stacktrace)
   }

   override fun start() {
      super.start()
      log.info(TAG, "Web server is up")

      LocalBroadcastManager
         .getInstance(context)
         .sendBroadcast(Intent(MainActivity.CHANGE_STATE).also {
            it.putExtra(MainActivity.STATE, ServiceState.RUNNING.name)
         })
   }

   override fun stop() {
      log.info(TAG, "Stopping web server...")
      super.stop()
      queuedEndpoints.forEach { it.shutdownQueue() }

      LocalBroadcastManager
         .getInstance(context)
         .sendBroadcast(Intent(MainActivity.CHANGE_STATE).also {
            it.putExtra(MainActivity.STATE, ServiceState.STOPPED.name)
         })
   }

   companion object {
      private const val TAG = "@webserver"
      private const val MIME_JSON = "application/json"
      const val DEFAULT_PORT = 8080
   }
}