package com.bartlomiejpluta.ttsserver.core.lua.lib

import com.vmichalak.sonoscontroller.SonosDevice
import com.vmichalak.sonoscontroller.SonosDiscovery
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.ZeroArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua

class OfFunction : OneArgFunction() {
   override fun call(ip: LuaValue): LuaValue =
      CoerceJavaToLua.coerce(SonosDevice(ip.checkjstring()))
}

class DiscoverFunction : ZeroArgFunction() {
   override fun call(): LuaTable = LuaValue.tableOf().also { devices ->
      SonosDiscovery.discover()
         .map { CoerceJavaToLua.coerce(it) }
         .forEachIndexed { i, device -> devices.set(i + 1, device) }
   }
}

class SonosLibrary : TwoArgFunction() {

   override fun call(modname: LuaValue, env: LuaValue): LuaValue {
      val sonos = LuaValue.tableOf().also {
         it.set("discover", DiscoverFunction())
         it.set("of", OfFunction())
      }

      env.set("sonos", sonos)

      return LuaValue.NIL
   }
}