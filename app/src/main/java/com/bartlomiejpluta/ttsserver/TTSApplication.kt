package com.bartlomiejpluta.ttsserver

import com.bartlomiejpluta.ttsserver.di.component.DaggerAppComponent
import dagger.android.support.DaggerApplication

class TTSApplication : DaggerApplication() {
   override fun applicationInjector() = DaggerAppComponent.builder().create(this).let {
      it.inject(this)
      it
   }
}