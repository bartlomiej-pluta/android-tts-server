package io.bartek.web

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status.*
import io.bartek.R
import io.bartek.tts.TTS
import org.json.JSONObject
import java.util.*

private data class TTSRequestData(val text: String, val language: Locale)


class TTSServer(port: Int, private val context: Context) : NanoHTTPD(port),
    TextToSpeech.OnInitListener {
    private val tts = TTS(context, this)

    override fun serve(session: IHTTPSession?): Response {
        try {
            return tryToServe(session)
        } catch (e: ResponseException) {
            throw e
        } catch (e: Exception) {
            throw ResponseException(INTERNAL_ERROR, e.toString(), e)
        }
    }

    private fun tryToServe(session: IHTTPSession?): Response {
        val (text, language) = getRequestData(validateRequest(session))
        val (stream, size) = tts.performTTS(text, language)
        return newFixedLengthResponse(OK, "audio/x-wav", stream, size)
    }

    private fun validateRequest(session: IHTTPSession?): IHTTPSession {
        if (session == null) {
            throw ResponseException(BAD_REQUEST, "")
        }

        if (session.uri != "/") {
            throw ResponseException(NOT_FOUND, "")
        }

        if (session.method != Method.POST) {
            throw ResponseException(METHOD_NOT_ALLOWED, "")
        }

        if (session.headers["content-type"]?.let { it != "application/json" } != false) {
            throw ResponseException(BAD_REQUEST, "")
        }


        return session
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
                it.putExtra("STATE", "STARTED")
            })
    }

    override fun stop() {
        super.stop()
        LocalBroadcastManager
            .getInstance(context)
            .sendBroadcast(Intent("io.bartek.web.server.CHANGE_STATE").also {
                it.putExtra("STATE", "STOPPED")
            })
    }
}