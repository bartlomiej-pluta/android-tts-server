package com.bartlomiejpluta.ttsserver.core.lua.sandbox

import com.bartlomiejpluta.ttsserver.core.lua.lib.HTTPLibrary
import com.bartlomiejpluta.ttsserver.core.lua.lib.SonosLibrary
import com.bartlomiejpluta.ttsserver.core.lua.lib.TTSLibrary
import com.bartlomiejpluta.ttsserver.core.lua.lib.UtilLibrary
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
   private val utilLibrary: UtilLibrary,
   private val httpLibrary: HTTPLibrary,
   private val ttsLibrary: TTSLibrary,
   private val sonosLibrary: SonosLibrary
) {
   fun createSandbox() = Globals().also {
      it.load(JseBaseLib())
      it.load(PackageLib())
      it.load(TableLib())
      it.load(StringLib())
      it.load(JseMathLib())
      it.load(JseOsLib())
      it.load(utilLibrary)
      it.load(httpLibrary)
      it.load(ttsLibrary)
      it.load(sonosLibrary)
      LoadState.install(it)
      LuaC.install(it)
   }
}