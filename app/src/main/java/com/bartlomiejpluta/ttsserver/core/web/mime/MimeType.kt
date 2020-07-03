package com.bartlomiejpluta.ttsserver.core.web.mime

import cafe.adriel.androidaudioconverter.model.AudioFormat
import java.io.File
import java.util.*

enum class MimeType(val mimeType: String) {
   AAC("audio/aac"),
   MP3("audio/mpeg"),
   M4A("audio/m4a"),
   WMA("audio/x-ms-wma"),
   WAV("audio/x-wav"),
   FLAC("audio/x-wav"),
   TEXT("text/plain"),
   JSON("application/json");

   companion object {
      fun forAudioFormat(audioFormat: AudioFormat) = when(audioFormat) {
         AudioFormat.AAC -> AAC
         AudioFormat.MP3 -> MP3
         AudioFormat.M4A -> M4A
         AudioFormat.WMA -> WMA
         AudioFormat.WAV -> WAV
         AudioFormat.FLAC -> FLAC
      }

      fun forFile(file: File) = valueOf(file.extension.toUpperCase(Locale.ROOT))
   }
}