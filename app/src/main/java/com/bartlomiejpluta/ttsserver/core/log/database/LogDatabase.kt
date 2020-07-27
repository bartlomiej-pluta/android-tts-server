package com.bartlomiejpluta.ttsserver.core.log.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bartlomiejpluta.ttsserver.core.log.dao.LogDao
import com.bartlomiejpluta.ttsserver.core.log.model.entity.LogEntry

@Database(entities = [LogEntry::class], version = 1)
abstract class LogDatabase : RoomDatabase() {
   abstract fun dao(): LogDao
}