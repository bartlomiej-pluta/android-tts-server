package com.bartlomiejpluta.ttsserver.core.lua.lib

import com.bartlomiejpluta.ttsserver.core.tts.engine.TTSEngine
import org.luaj.vm2.LuaNil
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction
import java.lang.IllegalArgumentException
import java.util.*

class TTSLibrary(private val ttsEngine: TTSEngine) : TwoArgFunction() {
   override fun call(modname: LuaValue, env: LuaValue): LuaValue {
      val tts = LuaValue.tableOf()
      tts.set("performTTS", SayMethod(ttsEngine))
      env.set("tts", tts)
      return tts
   }

   class SayMethod(private val ttsEngine: TTSEngine) : TwoArgFunction() {
      override fun call(textArg: LuaValue, languageArg: LuaValue): LuaValue {
         val text = textArg as? LuaString ?: throw IllegalArgumentException("Text should be a string")
         val language = textArg as? LuaString ?: throw IllegalArgumentException("Language should be a string")

         ttsEngine.performTTS(text.tojstring(), Locale.forLanguageTag(language.tojstring()))

         return LuaValue.NIL
      }

   }
}