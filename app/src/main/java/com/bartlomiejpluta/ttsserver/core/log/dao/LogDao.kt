package com.bartlomiejpluta.ttsserver.core.log.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.bartlomiejpluta.ttsserver.core.log.model.entity.LogEntry

@Dao
interface LogDao {

   @Insert
   fun insert(entry: LogEntry)

   @Query("SELECT * FROM logentry ORDER BY date DESC LIMIT 5000")
   fun getAll(): List<LogEntry>

   @Query("DELETE FROM logentry")
   fun clear()
}