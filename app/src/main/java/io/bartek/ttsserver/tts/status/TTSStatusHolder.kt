package io.bartek.ttsserver.tts.status

import android.speech.tts.TextToSpeech
import io.bartek.ttsserver.tts.status.TTSStatus

class TTSStatusHolder : TextToSpeech.OnInitListener {
   var status = TTSStatus.UNLOADED
      private set

   override fun onInit(status: Int) {
      this.status = TTSStatus.of(status)
   }
}