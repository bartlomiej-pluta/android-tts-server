package com.bartlomiejpluta.ttsserver.core.log.converter

import androidx.room.TypeConverter
import com.bartlomiejpluta.ttsserver.core.log.model.enumeration.LogLevel

object LogLevelConverter {

   @TypeConverter
   @JvmStatic
   fun toLogLevel(string: String?) = string?.let { LogLevel.valueOf(it) }

   @TypeConverter
   @JvmStatic
   fun fromLogLevel(logLevel: LogLevel?) = logLevel?.name
}