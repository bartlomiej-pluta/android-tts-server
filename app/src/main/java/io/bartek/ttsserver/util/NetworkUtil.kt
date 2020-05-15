package io.bartek.ttsserver.util

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import java.net.InetAddress


class NetworkUtil(private val context: Context) {
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