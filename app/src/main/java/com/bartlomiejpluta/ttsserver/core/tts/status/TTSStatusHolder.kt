package com.bartlomiejpluta.ttsserver.core.tts.status

import android.speech.tts.TextToSpeech

class TTSStatusHolder : TextToSpeech.OnInitListener {
   var status = TTSStatus.UNLOADED
      private set

   override fun onInit(status: Int) {
      this.status = TTSStatus.of(status)
   }
}