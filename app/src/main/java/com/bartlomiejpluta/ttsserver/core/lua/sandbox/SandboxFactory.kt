package com.bartlomiejpluta.ttsserver.core.lua.sandbox

import com.bartlomiejpluta.ttsserver.core.lua.lib.HTTPLibrary
import com.bartlomiejpluta.ttsserver.core.lua.lib.TTSLibrary
import org.luaj.vm2.Globals
import org.luaj.vm2.LoadState
import org.luaj.vm2.compiler.LuaC
import org.luaj.vm2.lib.PackageLib
import org.luaj.vm2.lib.StringLib
import org.luaj.vm2.lib.TableLib
import org.luaj.vm2.lib.jse.JseBaseLib
import org.luaj.vm2.lib.jse.JseMathLib
import org.luaj.vm2.lib.jse.JseOsLib

class SandboxFactory(
   private val httpLibrary: HTTPLibrary,
   private val ttsLibrary: TTSLibrary
) {
   fun createSandbox() = Globals().also {
      it.load(JseBaseLib())
      it.load(PackageLib())
      it.load(TableLib())
      it.load(StringLib())
      it.load(JseMathLib())
      it.load(JseOsLib())
      it.load(httpLibrary)
      it.load(ttsLibrary)
      LoadState.install(it)
      LuaC.install(it)
   }
}