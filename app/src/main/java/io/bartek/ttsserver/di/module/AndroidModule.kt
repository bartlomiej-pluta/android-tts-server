package io.bartek.ttsserver.di.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.bartek.ttsserver.ui.main.MainActivity
import io.bartek.ttsserver.ui.help.HelpActivity
import io.bartek.ttsserver.ui.preference.PreferencesActivity
import io.bartek.ttsserver.service.foreground.ForegroundService

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