package io.bartek.web

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status.*
import io.bartek.R
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.HashMap

data class TTSRequestData(val text: String, val language: Locale)

data class Lock(var success: Boolean = false) : Object()

data class SpeechData(val stream: InputStream, val size: Long)

class TTSProcessListener(private val uuid: String, private val lock: Lock) :
    UtteranceProgressListener() {
    override fun onDone(utteranceId: String?) {
        if (utteranceId == uuid) {
            synchronized(lock) {
                lock.success = true
                lock.notifyAll()
            }
        }
    }

    override fun onError(utteranceId: String?) {
        if (utteranceId == uuid) {
            synchronized(lock) {
                lock.success = false
                lock.notifyAll()
            }
        }
    }

    override fun onStart(utteranceId: String?) {}
}

class TTSServer(port: Int, private val context: Context) : NanoHTTPD(port),
    TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context, this)

    override fun serve(session: IHTTPSession?): Response {
        try {
            val (text, language) = getRequestData(validateRequest(session))
            val (stream, size) = performTTS(text, language)
            return newFixedLengthResponse(OK, "audio/x-wav", stream, size)
        } catch (e: ResponseException) {
            throw e
        } catch (e: Exception) {
            throw ResponseException(INTERNAL_ERROR, e.toString(), e)
        }
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

        if (session.headers["content-type"] ?. let { it != "application/json" } != false) {
            throw ResponseException(BAD_REQUEST, "")
        }


        return session
    }

    private fun performTTS(text: String, language: Locale): SpeechData {
        val file = createTempFile("tmp_tts_server", ".wav")

        val uuid = UUID.randomUUID().toString()
        val lock = Lock()
        tts.setOnUtteranceProgressListener(TTSProcessListener(uuid, lock))

        synchronized(lock) {
            tts.language = language
            tts.synthesizeToFile(text, null, file, uuid)
            lock.wait()
        }

        if (!lock.success) {
            throw RuntimeException("TTS failed")
        }

        val stream = BufferedInputStream(FileInputStream(file))
        val length = file.length()

        file.delete()

        return SpeechData(stream, length)
    }

    private fun getRequestData(session: IHTTPSession): TTSRequestData {
        val map = mutableMapOf<String, String>()
        session.parseBody(map)
        val json = JSONObject(map["postData"] ?: "{}")
        val language = Locale(json.optString("language", "en_US"))
        val text = json.optString("text") ?: throw ResponseException(
            BAD_REQUEST,
            "The missing 'text' field is required."
        )
        return TTSRequestData(text, language)
    }

    override fun onInit(status: Int) = start()

    override fun start() {
        super.start()
        Toast.makeText(
            context,
            context.resources.getString(R.string.server_toast_service_started),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun stop() {
        super.stop()
        Toast.makeText(
            context,
            context.resources.getString(R.string.server_toast_service_stopped),
            Toast.LENGTH_SHORT
        ).show()
    }
}