package com.bartlomiejpluta.ttsserver.core.tts.engine

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.speech.tts.TextToSpeech
import com.bartlomiejpluta.ttsserver.core.tts.exception.TTSException
import com.bartlomiejpluta.ttsserver.core.tts.listener.GongListener
import com.bartlomiejpluta.ttsserver.core.tts.listener.TTSProcessListener
import com.bartlomiejpluta.ttsserver.core.tts.model.TTSStream
import com.bartlomiejpluta.ttsserver.core.tts.status.TTSStatus
import com.bartlomiejpluta.ttsserver.core.tts.status.TTSStatusHolder
import com.bartlomiejpluta.ttsserver.ui.preference.PreferenceKey
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.*

class TTSEngine(
   private val context: Context,
   private val tts: TextToSpeech,
   private val ttsStatusHolder: TTSStatusHolder,
   private val preferences: SharedPreferences
) {
   private val messageDigest = MessageDigest.getInstance("SHA-256")

   val status: TTSStatus
      get() = ttsStatusHolder.status

   fun createTTSFile(text: String, language: Locale): File {
      val digest = hash(text, language)
      val filename = "tts_$digest.wav"
      val file = File(context.cacheDir, filename)

      file.takeIf { it.exists() }?.let { return it }

      val uuid = UUID.randomUUID().toString()
      val listener = TTSProcessListener(uuid)
      tts.setOnUtteranceProgressListener(listener)

      tts.language = language
      tts.synthesizeToFile(text, null, file, uuid)
      listener.await()

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
      val listener = TTSProcessListener(uuid)
      tts.setOnUtteranceProgressListener(listener)

      tts.language = language
      tts.synthesizeToFile(text, null, file, uuid)
      listener.await()

      val stream = BufferedInputStream(FileInputStream(file))
      val length = file.length()

      file.delete()

      return TTSStream(stream, length)
   }

   fun performTTS(text: String, language: Locale) {
      val uuid = UUID.randomUUID().toString()
      val listener = TTSProcessListener(uuid)
      tts.setOnUtteranceProgressListener(listener)

      tts.language = language
      playGong()
      tts.speak(text, TextToSpeech.QUEUE_ADD, null, uuid)
      listener.await()
   }

   private fun playGong() {
      if (!preferences.getBoolean(PreferenceKey.ENABLE_GONG, false)) {
         return
      }

      val listener = GongListener()
      val uri = preferences.getString(PreferenceKey.GONG, null) ?: throw TTSException()

      MediaPlayer().apply {
         setOnCompletionListener(listener)
         setAudioAttributes(gongAudioAttributes)
         setDataSource(context, Uri.parse(uri))
         prepare()
         start()
         listener.await()
      }
   }

   companion object {
      private val gongAudioAttributes = AudioAttributes.Builder()
         .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
         .build()
   }
}

