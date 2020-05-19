package com.bartlomiejpluta.ttsserver.di.component

import android.content.Context
import com.bartlomiejpluta.ttsserver.TTSApplication
import com.bartlomiejpluta.ttsserver.di.module.AndroidModule
import com.bartlomiejpluta.ttsserver.di.module.TTSModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, AndroidModule::class, TTSModule::class])
interface AppComponent : AndroidInjector<TTSApplication> {

   @Component.Builder
   abstract class Builder : AndroidInjector.Builder<TTSApplication>() {

      @BindsInstance
      abstract fun appContext(context: Context)

      override fun seedInstance(instance: TTSApplication) = appContext(instance)
   }
}