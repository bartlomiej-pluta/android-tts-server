package io.bartek.ttsserver.core.tts.status

import android.speech.tts.TextToSpeech

enum class TTSStatus(private val status: Int) {
   READY(TextToSpeech.SUCCESS),
   ERROR(TextToSpeech.ERROR),
   UNLOADED(1);

   companion object {
      fun of(status: Int) = values().firstOrNull { it.status == status } ?: UNLOADED
   }
}