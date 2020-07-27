package com.bartlomiejpluta.ttsserver.initializer

import android.content.Context
import android.content.SharedPreferences
import com.bartlomiejpluta.ttsserver.R
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream

class ScriptsInitializer(private val context: Context, private val preferences: SharedPreferences) {
   private val endpointsDirectory: File?
      get() = context.getExternalFilesDir("endpoints")

   private val configDirectory: File?
      get() = context.getExternalFilesDir("config")

   fun initializeOnce() {
      if(!preferences.getBoolean(INITIALIZED_FLAG, false)) {
         initialize()

         preferences.edit().apply {
            putBoolean(INITIALIZED_FLAG, true)
            apply()
         }
      }
   }

   fun initialize() {
      endpointsDirectory?.listFiles()?.forEach { it.delete() }
      configDirectory?.listFiles()?.forEach { it.delete() }
      initializeConfig()
      initializeEndpoints()
   }

   private fun initializeConfig() =
      saveToFile("config.lua", configDirectory, R.raw.config)

   private fun initializeEndpoints() =
      endpoints.forEach { saveToFile(it.key, endpointsDirectory, it.value) }

   private fun saveToFile(fileName: String, directory: File?, resourceId: Int) {
      BufferedInputStream(context.resources.openRawResource(resourceId)).use { input ->
         FileOutputStream(File(directory, fileName)).use { output ->
            input.copyTo(output)
         }
      }
   }

   companion object {
      private const val INITIALIZED_FLAG = "flag_initialized"
      private val endpoints = mapOf(
         "say.lua" to R.raw.say,
         "file.lua" to R.raw.file,
         "sonos.lua" to R.raw.sonos,
         "cache.lua" to R.raw.cache
      )
   }
}