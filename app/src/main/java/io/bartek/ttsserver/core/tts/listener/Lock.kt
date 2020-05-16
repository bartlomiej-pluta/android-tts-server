package io.bartek.ttsserver.core.tts.listener

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
// TODO: Investigate the Kotlin way to achieve the same
data class Lock(var success: Boolean = false) : Object()