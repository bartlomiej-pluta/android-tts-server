package com.bartlomiejpluta.ttsserver.core.log.service

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bartlomiejpluta.ttsserver.core.log.database.LogDatabase
import com.bartlomiejpluta.ttsserver.core.log.model.entity.LogEntry
import com.bartlomiejpluta.ttsserver.core.log.model.enumeration.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.threeten.bp.Instant

class LogService(private val context: Context, logDatabase: LogDatabase) {
   private val dao = logDatabase.dao()
   private val broadcastManager = LocalBroadcastManager.getInstance(context)

   fun debug(source: String, message: String) = log(LogLevel.DEBUG, source, message)

   fun info(source: String, message: String) = log(LogLevel.INFO, source, message)

   fun warn(source: String, message: String) = log(LogLevel.WARN, source, message)

   fun error(source: String, message: String) = log(LogLevel.ERROR, source, message)

   fun fatal(source: String, message: String) = log(LogLevel.FATAL, source, message)

   fun log(level: LogLevel, source: String, message: String) =
      log(LogEntry(null, Instant.now(), level, source, message))

   fun log(logEntry: LogEntry) {
      persistLogEntry(logEntry)
      broadcastLogEntry()
   }

   private fun persistLogEntry(logEntry: LogEntry) = runBlocking {
      withContext(Dispatchers.IO + Job()) {
         dao.insert(logEntry)
      }
   }

   private fun broadcastLogEntry() = broadcastManager.sendBroadcast(Intent(UPDATE_LOGS))

   val allLogs: List<LogEntry>
      get() = dao.getAll()

   fun clearLogs() = dao.clear()

   companion object {
      const val UPDATE_LOGS = "UPDATE_LOGS"
   }
}