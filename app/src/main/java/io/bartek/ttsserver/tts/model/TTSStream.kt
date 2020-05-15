package io.bartek.ttsserver.tts.model

import java.io.InputStream

data class TTSStream(val stream: InputStream, val length: Long)