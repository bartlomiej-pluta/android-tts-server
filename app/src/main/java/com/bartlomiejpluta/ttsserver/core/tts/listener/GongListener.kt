package com.bartlomiejpluta.ttsserver.core.tts.listener

import android.media.MediaPlayer
import java.util.concurrent.LinkedBlockingQueue

class GongListener : MediaPlayer.OnCompletionListener {
   private val queue = LinkedBlockingQueue<Any>()

   override fun onCompletion(mp: MediaPlayer?) {
      queue.add(Any())
   }

   fun await() = queue.take()
}