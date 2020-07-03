package com.bartlomiejpluta.ttsserver.core.tts.engine

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.speech.tts.TextToSpeech
import cafe.adriel.androidaudioconverter.model.AudioFormat
import com.bartlomiejpluta.ttsserver.core.tts.exception.TTSException
import com.bartlomiejpluta.ttsserver.core.tts.listener.GongListener
import com.bartlomiejpluta.ttsserver.core.tts.listener.TTSProcessListener
import com.bartlomiejpluta.ttsserver.core.tts.status.TTSStatus
import com.bartlomiejpluta.ttsserver.core.tts.status.TTSStatusHolder
import com.bartlomiejpluta.ttsserver.core.util.AudioConverter
import com.bartlomiejpluta.ttsserver.ui.preference.key.PreferenceKey
import java.io.File
import java.security.MessageDigest
import java.util.*

class TTSEngine(
   private val context: Context,
   private val tts: TextToSpeech,
   private val ttsStatusHolder: TTSStatusHolder,
   private val preferences: SharedPreferences,
   private val audioConverter: AudioConverter
) {
   private val messageDigest = MessageDigest.getInstance("SHA-256")

   val status: TTSStatus
      get() = ttsStatusHolder.status

   fun createTTSFile(
      text: String,
      language: Locale,
      audioFormat: AudioFormat = AudioFormat.WAV
   ): File {
      val digest = hash(text, language)
      val targetFilename = "tts_$digest.${audioFormat.format}"
      val wavFilename = "tts_$digest.wav"
      val wavFile = File(context.cacheDir, wavFilename)
      val targetFile = File(context.cacheDir, targetFilename)

      targetFile.takeIf { it.exists() }?.let { return it }

      val uuid = UUID.randomUUID().toString()
      val listener = TTSProcessListener(uuid)
      tts.setOnUtteranceProgressListener(listener)

      tts.language = language
      tts.synthesizeToFile(text, null, wavFile, uuid)
      listener.await()

      return convertFile(wavFile, audioFormat)
   }

   private fun convertFile(file: File, audioFormat: AudioFormat): File {
      if (audioFormat == AudioFormat.WAV) {
         return file
      }


      return audioConverter.convert(file, audioFormat).also { file.delete() }
   }

   private fun hash(text: String, language: Locale): String {
      val bytes = "$text$language".toByteArray()
      val digest = messageDigest.digest(bytes)
      return digest.fold("", { str, it -> str + "%02x".format(it) })
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

