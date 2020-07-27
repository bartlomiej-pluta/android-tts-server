package com.bartlomiejpluta.ttsserver.common.db.converter

import androidx.room.TypeConverter
import org.threeten.bp.Instant

object InstantConverter {

   @TypeConverter
   @JvmStatic
   fun toInstant(epoch: Long?) = epoch?.let { Instant.ofEpochMilli(it) }

   @TypeConverter
   @JvmStatic
   fun fromInstant(instant: Instant?) = instant?.toEpochMilli()
}