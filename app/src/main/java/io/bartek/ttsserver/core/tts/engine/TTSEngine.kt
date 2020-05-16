package io.bartek.ttsserver.core.tts.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import io.bartek.ttsserver.core.tts.exception.TTSException
import io.bartek.ttsserver.core.tts.listener.Lock
import io.bartek.ttsserver.core.tts.listener.TTSProcessListener
import io.bartek.ttsserver.core.tts.model.TTSStream
import io.bartek.ttsserver.core.tts.status.TTSStatus
import io.bartek.ttsserver.core.tts.status.TTSStatusHolder
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.*

class TTSEngine(
   private val context: Context,
   private val tts: TextToSpeech,
   private val ttsStatusHolder: TTSStatusHolder
) {
   private val messageDigest = MessageDigest.getInstance("SHA-256")

   val status: TTSStatus
      get() = ttsStatusHolder.status

   fun createTTSFile(text: String, language: Locale): File {
      val digest = hash(text, language)
      val filename = "tts_$digest.wav"
      val file = File(context.cacheDir, filename)

      file.takeIf { it.exists() } ?.let { return it }

      val uuid = UUID.randomUUID().toString()
      val lock = Lock()
      tts.setOnUtteranceProgressListener(
         TTSProcessListener(
            uuid,
            lock
         )
      )

      synchronized(lock) {
         tts.language = language
         tts.synthesizeToFile(text, null, file, uuid)
         lock.wait()
      }

      if (!lock.success) {
         throw TTSException()
      }

      return file
   }

   private fun hash(text: String, language: Locale): String {
      val bytes = "$text$language".toByteArray()
      val digest = messageDigest.digest(bytes)
      return digest.fold("", { str, it -> str + "%02x".format(it) })
   }

   fun fetchTTSStream(text: String, language: Locale): TTSStream {
      val file = createTempFile("tmp_tts_server", ".wav")

      val uuid = UUID.randomUUID().toString()
      val lock = Lock()
      tts.setOnUtteranceProgressListener(
         TTSProcessListener(
            uuid,
            lock
         )
      )

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

      return TTSStream(stream, length)
   }

   fun performTTS(text: String, language: Locale) {
      val uuid = UUID.randomUUID().toString()
      val lock = Lock()
      tts.setOnUtteranceProgressListener(
         TTSProcessListener(
            uuid,
            lock
         )
      )

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

