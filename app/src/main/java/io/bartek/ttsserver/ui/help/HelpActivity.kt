package io.bartek.ttsserver.ui.help

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import io.bartek.R
import java.util.*

class HelpActivity : AppCompatActivity() {
   private lateinit var helpView: WebView

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_help)
      helpView = findViewById(R.id.help_view)
      loadHelp()
   }

   private fun loadHelp() {
      val lang = Locale.getDefault().language
      val file = HELP_FILE.format(".$lang")
         .takeIf { resources.assets.list("help")?.contains(it) == true }
         ?: HELP_FILE.format("")
      helpView.loadUrl("file:///android_asset/help/${file}")
   }

   companion object {
      private const val HELP_FILE = "help%s.html"
   }
}
