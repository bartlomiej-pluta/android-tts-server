package com.bartlomiejpluta.ttsserver.core.tts.listener

import cafe.adriel.androidaudioconverter.callback.IConvertCallback
import com.bartlomiejpluta.ttsserver.core.tts.exception.AudioConversionException
import java.io.File
import java.util.concurrent.LinkedBlockingQueue

class ConverterListener : IConvertCallback {
   private val queue = LinkedBlockingQueue<File>()

   fun await() = queue.take()

   override fun onSuccess(convertedFile: File?) {
      queue.add(convertedFile)
   }

   override fun onFailure(error: Exception?) {
      error?.let { throw AudioConversionException("Conversion failed", error) }
   }
}