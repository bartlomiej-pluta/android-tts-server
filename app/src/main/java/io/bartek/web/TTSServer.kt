package io.bartek.web

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import fi.iki.elonen.NanoHTTPD
import io.bartek.R
import java.io.FileInputStream
import java.io.InputStream
import java.util.*

class TTSServer(port: Int, private val context: Context) : NanoHTTPD(port), TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context, this)

    override fun serve(session: IHTTPSession?): Response {
        tts.language = Locale("pl_PL")
        val uuid = UUID.randomUUID().toString()
        val file = createTempFile("tmp_tts_server", ".wav")
        val lock = Object()
        var error = false
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String?) {
                if(utteranceId == uuid) {
                    synchronized(lock) {
                        lock.notifyAll()
                    }
                }
            }

            override fun onError(utteranceId: String?) {
                if(utteranceId == uuid) {
                    error = true
                    synchronized(lock) {
                        lock.notifyAll()
                    }
                }
            }

            override fun onStart(utteranceId: String?) {}
        })

        synchronized(lock) {
            tts.synthesizeToFile(session?.uri, null, file, uuid)
            lock.wait()
        }

        val stream = FileInputStream(file)
        val length = file.length()

        Log.d("WAV", stream.toString())
        Log.d("WAV", length.toString())

        return when(error) {
            false -> newFixedLengthResponse(Response.Status.OK, "audio/x-wav", stream, length)
            else -> newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "TTS error on $uuid")
        }
    }

    override fun onInit(status: Int) = start()

    override fun start() {
        super.start()
        Toast.makeText(context, context.resources.getString(R.string.server_toast_service_started), Toast.LENGTH_SHORT).show()
    }

    override fun stop() {
        super.stop()
        Toast.makeText(context, context.resources.getString(R.string.server_toast_service_stopped), Toast.LENGTH_SHORT).show()
    }
}