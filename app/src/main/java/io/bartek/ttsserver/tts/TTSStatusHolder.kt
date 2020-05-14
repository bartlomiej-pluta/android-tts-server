package io.bartek.ttsserver.tts

import android.speech.tts.TextToSpeech

enum class TTSStatus(private val status: Int) {
   READY(TextToSpeech.SUCCESS),
   ERROR(TextToSpeech.ERROR),
   UNLOADED(1);

   companion object {
      fun of(status: Int) = values().firstOrNull { it.status == status } ?: UNLOADED
   }
}

class TTSStatusHolder : TextToSpeech.OnInitListener {
   var status = TTSStatus.UNLOADED
      private set

   override fun onInit(status: Int) {
      this.status = TTSStatus.of(status)
   }
}