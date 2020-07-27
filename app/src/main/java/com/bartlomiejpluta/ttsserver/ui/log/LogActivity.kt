package com.bartlomiejpluta.ttsserver.ui.log

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bartlomiejpluta.ttsserver.R
import com.bartlomiejpluta.ttsserver.core.log.model.entity.LogEntry
import com.bartlomiejpluta.ttsserver.core.log.model.enumeration.LogLevel
import com.bartlomiejpluta.ttsserver.core.log.service.LogService
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*
import javax.inject.Inject


class LogActivity : DaggerAppCompatActivity() {
   private val formatter = DateTimeFormatter
      .ofLocalizedDateTime(FormatStyle.MEDIUM)
      .withLocale(Locale.getDefault())
      .withZone(ZoneId.systemDefault())

   private lateinit var logView: WebView

   @Inject
   lateinit var logService: LogService

   private val receiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
         when (intent?.action) {
            LogService.UPDATE_LOGS -> updateLogs()
            else -> throw UnsupportedOperationException("This action is not supported")
         }
      }
   }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_log)
      logView = findViewById(R.id.logview)
      logView.webViewClient = object : WebViewClient() {
         override fun onPageCommitVisible(view: WebView?, url: String?) {
            view?.scrollY = view?.contentHeight ?: 0
            //view?.pageDown(true)
         }
      }
   }

   override fun onResume() {
      super.onResume()

      super.onResume()
      val filter = IntentFilter().apply {
         addAction(LogService.UPDATE_LOGS)
      }

      LocalBroadcastManager
         .getInstance(this)
         .registerReceiver(receiver, filter)

      updateLogs()
   }

   private fun updateLogs() {
      val logs = fetchLogs()
      val logsHTML = logsToHTML(logs)
      val html = LOGS_STYLES + logsHTML
      logView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");
   }

   private fun fetchLogs() = runBlocking {
      withContext(Dispatchers.IO + Job()) {
         logService.allLogs.asReversed()
      }
   }

   private fun logsToHTML(logs: List<LogEntry>) = logs.joinToString("<br />") {
      String.format(
         "%s <span style=\"color: %s\">%s</span> [ %s ] %s",
         formatter.format(it.date).padEnd(23, ' ').replace(" ", "&nbsp;"),
         logLevelColor(it.level),
         it.level.name.padEnd(5, ' ').replace(" ", "&nbsp;"),
         it.source.padEnd(15, ' ').replace(" ", "&nbsp;"),
         it.message
      )
   }

   private fun logLevelColor(level: LogLevel) = when (level) {
      LogLevel.DEBUG -> "#0044FF"
      LogLevel.INFO -> "#00FF00"
      LogLevel.WARN -> "#FFEB3B"
      LogLevel.ERROR -> "#FF7777"
      LogLevel.FATAL -> "#FF0000"
   }

   override fun onPause() {
      LocalBroadcastManager
         .getInstance(this)
         .unregisterReceiver(receiver)
      super.onPause()
   }

   override fun onCreateOptionsMenu(menu: Menu?): Boolean {
      menuInflater.inflate(R.menu.menu_logs, menu)
      return true
   }

   override fun onOptionsItemSelected(item: MenuItem): Boolean {
      when (item.itemId) {
         R.id.clear_logs -> clearLogs()
      }

      return super.onOptionsItemSelected(item)
   }

   private fun clearLogs() = AlertDialog.Builder(this)
      .setTitle(getString(R.string.dialog_confirmation))
      .setMessage(getString(R.string.dialog_clear_logs_confirmation))
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setPositiveButton(android.R.string.yes) { _, _ ->
         runBlocking {
            withContext(Dispatchers.IO + Job()) {
               logService.clearLogs()
            }
         }.also { updateLogs() }
      }
      .setNegativeButton(android.R.string.no, null).show()

   companion object {
      private const val LOGS_STYLES = "<style> * { font-family: monospace; } </style>"
   }
}