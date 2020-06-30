package com.bartlomiejpluta.ttsserver.di.module

import com.bartlomiejpluta.ttsserver.service.foreground.ForegroundService
import com.bartlomiejpluta.ttsserver.ui.help.HelpActivity
import com.bartlomiejpluta.ttsserver.ui.main.MainActivity
import com.bartlomiejpluta.ttsserver.ui.preference.component.PreferencesActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

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