package io.bartek.ttsserver.core.tts.listener

import android.speech.tts.UtteranceProgressListener

class TTSProcessListener(
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