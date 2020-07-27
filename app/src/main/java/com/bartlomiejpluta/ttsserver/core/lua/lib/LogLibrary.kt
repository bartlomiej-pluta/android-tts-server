package com.bartlomiejpluta.ttsserver.core.lua.lib

import com.bartlomiejpluta.ttsserver.core.log.model.enumeration.LogLevel
import com.bartlomiejpluta.ttsserver.core.log.service.LogService
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction

class LogLibrary(private val logService: LogService) : TwoArgFunction() {
   override fun call(modname: LuaValue, env: LuaValue): LuaValue {
      val scriptName = env.get("_G").get("script").checkjstring()
      val log = LuaValue.tableOf()
      log.set("debug", LogFunction(logService, LogLevel.DEBUG, scriptName))
      log.set("info", LogFunction(logService, LogLevel.INFO, scriptName))
      log.set("warn", LogFunction(logService, LogLevel.WARN, scriptName))
      log.set("error", LogFunction(logService, LogLevel.ERROR, scriptName))
      log.set("fatal", LogFunction(logService, LogLevel.FATAL, scriptName))
      env.set("log", log)

      return env
   }

   class LogFunction(
      private val log: LogService,
      private val level: LogLevel,
      private val scriptName: String
   ) : OneArgFunction() {
      override fun call(message: LuaValue): LuaValue {
         log.log(level, scriptName, message.checkjstring())
         return LuaValue.NIL
      }
   }
}