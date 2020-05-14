package io.bartek.ttsserver.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import io.bartek.ttsserver.exception.TTSException
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.*

data class SpeechData(val stream: InputStream, val size: Long)

class TTS(private val context: Context, initListener: TextToSpeech.OnInitListener) {
   private val tts = TextToSpeech(context, initListener)
   private val messageDigest = MessageDigest.getInstance("SHA-256")

   fun createTTSFile(text: String, language: Locale): File {
      val digest = hash(text, language)
      val filename = "tts_$digest.wav"
      val file = File(context.cacheDir, filename)

      val uuid = UUID.randomUUID().toString()
      val lock = Lock()
      tts.setOnUtteranceProgressListener(TTSProcessListener(uuid, lock))

      synchronized(lock) {
         tts.language = language
         tts.synthesizeToFile(text, null, file, uuid)
         lock.wait()
      }

      if(!lock.success) {
         throw TTSException()
      }

      return file
   }

   private fun hash(text: String, language: Locale): String {
      val bytes = "$text$language".toByteArray()
      val digest = messageDigest.digest(bytes)
      return digest.fold("", { str, it -> str + "%02x".format(it) })
   }

   fun fetchTTSStream(text: String, language: Locale): SpeechData {
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
         throw TTSException()
      }

      val stream = BufferedInputStream(FileInputStream(file))
      val length = file.length()

      file.delete()

      return SpeechData(stream, length)
   }

   fun performTTS(text: String, language: Locale) {
      val uuid = UUID.randomUUID().toString()
      val lock = Lock()
      tts.setOnUtteranceProgressListener(TTSProcessListener(uuid, lock))

      synchronized(lock) {
         tts.language = language
         tts.speak(text, TextToSpeech.QUEUE_ADD, null, uuid)
         lock.wait()
      }

      if (!lock.success) {
         throw TTSException()
      }
   }
}

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
// TODO: Investigate the Kotlin way to achieve the same
private data class Lock(var success: Boolean = false) : Object()

private class TTSProcessListener(
   private val uuid: String,
   private val lock: Lock
) : UtteranceProgressListener() {

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
