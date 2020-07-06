package com.bartlomiejpluta.ttsserver.core.lua.lib

import cafe.adriel.androidaudioconverter.model.AudioFormat
import com.bartlomiejpluta.ttsserver.core.tts.engine.TTSEngine
import org.luaj.vm2.LuaNil
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.ThreeArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua
import java.util.*

class TTSLibrary(private val ttsEngine: TTSEngine) : TwoArgFunction() {
   override fun call(modname: LuaValue, env: LuaValue): LuaValue {
      val tts = LuaValue.tableOf().apply {
         set("say", SayMethod(ttsEngine))
         set("sayToFile", FileMethod(ttsEngine))
      }

      env.set("tts", tts)

      val audioFormats = LuaValue.tableOf()
      AudioFormat.values().forEach { audioFormats.set(it.name, it.name) }
      env.set("AudioFormat", audioFormats)

      return LuaValue.NIL
   }

   class SayMethod(private val ttsEngine: TTSEngine) : TwoArgFunction() {
      override fun call(text: LuaValue, language: LuaValue): LuaValue {
         val locale = Locale.forLanguageTag(language.checkjstring().toUpperCase(Locale.ROOT))
         ttsEngine.performTTS(text.checkjstring(), locale)

         return LuaValue.NIL
      }
   }

   class FileMethod(private val ttsEngine: TTSEngine) : ThreeArgFunction() {
      override fun call(text: LuaValue, language: LuaValue, format: LuaValue): LuaValue {
         val lang = Locale.forLanguageTag(language.checkjstring())
         val audioFormat = format
            .takeIf { it !is LuaNil }
            ?.let { AudioFormat.valueOf(it.checkjstring()) }
            ?: AudioFormat.WAV

         val file = ttsEngine.createTTSFile(text.checkjstring(), lang, audioFormat)

         return CoerceJavaToLua.coerce(file)
      }
   }
}