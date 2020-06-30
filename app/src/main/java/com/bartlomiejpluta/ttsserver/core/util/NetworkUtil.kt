package com.bartlomiejpluta.ttsserver.core.util

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.content.SharedPreferences
import android.net.wifi.WifiManager
import com.bartlomiejpluta.ttsserver.ui.preference.key.PreferenceKey
import java.net.InetAddress


class NetworkUtil(private val context: Context, private val preferences: SharedPreferences) {
   val serverAddress: String
      get() = "http://${getIpAddress()}:${preferences.getInt(PreferenceKey.PORT, 8000)}"

   fun getIpAddress(): String {
      return (context.getApplicationContext().getSystemService(WIFI_SERVICE) as WifiManager).let {
         inetAddress(it.dhcpInfo.ipAddress).toString().substring(1)
      }
   }

   private fun inetAddress(hostAddress: Int) = byteArrayOf(
      (0xff and hostAddress).toByte(),
      (0xff and (hostAddress shr 8)).toByte(),
      (0xff and (hostAddress shr 16)).toByte(),
      (0xff and (hostAddress shr 24)).toByte()
   ).let { InetAddress.getByAddress(it) }
}