package io.bartek.ttsserver

import dagger.android.support.DaggerApplication
import io.bartek.ttsserver.di.component.DaggerAppComponent

class TTSApplication : DaggerApplication() {
   override fun applicationInjector() = DaggerAppComponent.builder().create(this).let {
      it.inject(this)
      it
   }
}