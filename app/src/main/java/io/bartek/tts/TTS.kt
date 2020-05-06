package io.bartek.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.lang.RuntimeException
import java.util.*

data class SpeechData(val stream: InputStream, val size: Long)

class TTS(context: Context, initListener: TextToSpeech.OnInitListener) {
    private val tts = TextToSpeech(context, initListener)

    fun performTTS(text: String, language: Locale): SpeechData {
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
}

private data class Lock(var success: Boolean = false) : Object()

private class TTSProcessListener(private val uuid: String, private val lock: Lock) :
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
