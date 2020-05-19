package com.bartlomiejpluta.ttsserver.core.tts.listener

import android.speech.tts.UtteranceProgressListener
import com.bartlomiejpluta.ttsserver.core.tts.exception.TTSException
import java.util.concurrent.LinkedBlockingQueue

class TTSProcessListener(private val uuid: String) : UtteranceProgressListener() {
   private val queue = LinkedBlockingQueue<Boolean>()

   override fun onDone(utteranceId: String?) {
      if (utteranceId == uuid) {
         queue.add(true)
      }
   }

   override fun onError(utteranceId: String?) {
      if (utteranceId == uuid) {
         queue.add(false)
      }
   }

   override fun onStart(utteranceId: String?) {}

   fun await() {
      if(!queue.take()) {
         throw TTSException()
      }
   }
}