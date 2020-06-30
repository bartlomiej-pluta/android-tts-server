package com.bartlomiejpluta.ttsserver.ui.preference.component

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bartlomiejpluta.R

class PreferencesActivity : AppCompatActivity() {

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_preferences)
      supportFragmentManager
         .beginTransaction()
         .replace(R.id.preferences,
            PreferencesFragment()
         )
         .commit()
      supportActionBar?.setDisplayHomeAsUpEnabled(true)
   }
}