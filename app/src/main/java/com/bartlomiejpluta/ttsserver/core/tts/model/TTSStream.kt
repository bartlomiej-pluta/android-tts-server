package com.bartlomiejpluta.ttsserver.core.tts.model

import java.io.InputStream

data class TTSStream(val stream: InputStream, val length: Long)