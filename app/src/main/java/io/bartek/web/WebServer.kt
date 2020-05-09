package io.bartek.web

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status.*
import io.bartek.service.ServiceState
import io.bartek.tts.TTS
import org.json.JSONObject
import java.util.*

private data class TTSRequestData(val text: String, val language: Locale)


class TTSServer(port: Int, private val context: Context) : NanoHTTPD(port),
    TextToSpeech.OnInitListener {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val tts = TTS(context, this)

    override fun serve(session: IHTTPSession?): Response {
        try {
            session?.let {
                return when(it.uri) {
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
        if(!preferences.getBoolean("preference_enable_wave_endpoint", true)) {
            throw ResponseException(NOT_FOUND, "")
        }

        if (session.method != Method.POST) {
            throw ResponseException(METHOD_NOT_ALLOWED, "")
        }

        if (session.headers["content-type"]?.let { it != "application/json" } != false) {
            throw ResponseException(BAD_REQUEST, "")
        }

        val (text, language) = getRequestData(session)
        val (stream, size) = tts.fetchTTSStream(text, language)
        return newFixedLengthResponse(OK, "audio/x-wav", stream, size)
    }

    private fun say(session: IHTTPSession): Response {
        if(!preferences.getBoolean("preference_enable_say_endpoint", true)) {
            throw ResponseException(NOT_FOUND, "")
        }

        if (session.method != Method.POST) {
            throw ResponseException(METHOD_NOT_ALLOWED, "")
        }

        if (session.headers["content-type"]?.let { it != "application/json" } != false) {
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
        val language = Locale(json.optString("language", "en_US"))
        val text = json.optString("text") ?: throw ResponseException(BAD_REQUEST, "")
        return TTSRequestData(text, language)
    }

    override fun onInit(status: Int) = start()

    override fun start() {
        super.start()
        LocalBroadcastManager
            .getInstance(context)
            .sendBroadcast(Intent("io.bartek.web.server.CHANGE_STATE").also {
                it.putExtra("STATE", ServiceState.RUNNING.name)
            })
    }

    override fun stop() {
        super.stop()
        LocalBroadcastManager
            .getInstance(context)
            .sendBroadcast(Intent("io.bartek.web.server.CHANGE_STATE").also {
                it.putExtra("STATE", ServiceState.STOPPED.name)
            })
    }
}