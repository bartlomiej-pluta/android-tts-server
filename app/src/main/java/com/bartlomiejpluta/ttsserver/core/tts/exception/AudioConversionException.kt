package com.bartlomiejpluta.ttsserver.core.tts.exception

class AudioConversionException : Exception {
   constructor(message: String) : super(message)
   constructor(message: String, cause: Throwable) : super(message, cause)
}