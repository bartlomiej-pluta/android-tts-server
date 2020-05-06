package io.bartek.web

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import fi.iki.elonen.NanoHTTPD
import java.util.*

class TTSServer(port: Int, private val context: Context) : NanoHTTPD(port), TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context, this)

    override fun serve(session: IHTTPSession?): Response {
        tts.language = Locale("pl_PL")
        tts.speak(session?.uri, TextToSpeech.QUEUE_ADD, null, "")

        return newFixedLengthResponse(""" { "message": "ok" } """)
    }

    override fun onInit(status: Int) = start()

    override fun start() {
        super.start()
        Toast.makeText(context, "TTS-HTTP Server started", Toast.LENGTH_SHORT).show()
    }

    override fun stop() {
        super.stop()
        Toast.makeText(context, "TTS-HTTP Server stopped", Toast.LENGTH_SHORT).show()
    }
}