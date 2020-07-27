package com.bartlomiejpluta.ttsserver.core.log.model.entity

import androidx.room.*
import com.bartlomiejpluta.ttsserver.common.db.converter.InstantConverter
import com.bartlomiejpluta.ttsserver.core.log.converter.LogLevelConverter
import com.bartlomiejpluta.ttsserver.core.log.model.enumeration.LogLevel
import org.threeten.bp.Instant

@Entity
@TypeConverters(InstantConverter::class, LogLevelConverter::class)
data class LogEntry (
   @PrimaryKey
   val id: Int?,

   @ColumnInfo(name = "date")
   val date: Instant,

   @ColumnInfo(name = "level")
   val level: LogLevel,

   @ColumnInfo(name = "source")
   val source: String,

   @ColumnInfo(name = "message")
   val message: String
)