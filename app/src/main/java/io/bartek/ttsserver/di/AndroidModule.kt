package io.bartek.ttsserver.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.bartek.ttsserver.MainActivity
import io.bartek.ttsserver.help.HelpActivity
import io.bartek.ttsserver.preference.PreferencesActivity
import io.bartek.ttsserver.service.ForegroundService

@Module
abstract class AndroidModule {

   @ContributesAndroidInjector
   abstract fun mainActivity(): MainActivity

   @ContributesAndroidInjector
   abstract fun helpActivity(): HelpActivity

   @ContributesAndroidInjector
   abstract fun preferencesActivity(): PreferencesActivity

   @ContributesAndroidInjector
   abstract fun foregroundService(): ForegroundService
}