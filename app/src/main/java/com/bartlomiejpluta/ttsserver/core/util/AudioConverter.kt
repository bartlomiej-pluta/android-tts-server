package com.bartlomiejpluta.ttsserver.core.util

import android.content.Context
import cafe.adriel.androidaudioconverter.AndroidAudioConverter
import cafe.adriel.androidaudioconverter.callback.ILoadCallback
import cafe.adriel.androidaudioconverter.model.AudioFormat
import com.bartlomiejpluta.ttsserver.core.tts.exception.AudioConversionException
import com.bartlomiejpluta.ttsserver.core.tts.listener.ConverterListener
import java.io.File

class AudioConverter(private val context: Context) {
   var state: State = State.UNLOADED
      private set

   enum class State {
      READY,
      ERROR,
      UNLOADED
   }

   init {
      AndroidAudioConverter.load(context, object : ILoadCallback {
         override fun onSuccess() {
            state = State.READY
         }

         override fun onFailure(error: Exception?) {
            state = State.ERROR
         }
      })
   }

   fun convert(file: File, format: AudioFormat): File {
      if(state != State.READY) {
         throw AudioConversionException("Converter is not ready")
      }

      val listener = ConverterListener()
      AndroidAudioConverter.with(context)
         .setFile(file)
         .setFormat(format)
         .setCallback(listener)
         .convert()

      return listener.await()
   }
}