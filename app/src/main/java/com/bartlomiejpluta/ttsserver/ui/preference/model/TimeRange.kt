package com.bartlomiejpluta.ttsserver.ui.preference.model

import java.util.*

data class TimeRange(val beginHour: Int, val beginMinute: Int, val endHour: Int, val endMinute: Int) {
   private val begin: Int
   get() = beginHour * 60 + beginMinute

   private val end: Int
   get() = endHour * 60 + endMinute

   fun inRange(calendar: Calendar): Boolean {
      val current = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
      return when {
         begin <= end -> current in begin..end
         else -> !(current in end..begin)
      }
   }

   override fun toString() = "$beginHour:$beginMinute-$endHour:$endMinute"

   companion object {
      fun parse(range: String): TimeRange {
         val (beginTime, endTime) = range.splitAndMap("-") { it }
         val (beginHour, beginMinute) = beginTime.splitAndMap(":") { it.toInt() }
         val (endHour, endMinute) = endTime.splitAndMap(":") { it.toInt() }
         return TimeRange(
            beginHour,
            beginMinute,
            endHour,
            endMinute
         )
      }

      private fun <T> String.splitAndMap(delimiter: String, mapper: (String) -> T): List<T> {
         return this.split(delimiter)
            .map { mapper(it) }
            .takeIf { it.size == 2 }
            ?: throw IllegalArgumentException("Expected format: HH:mm-HH:mm")
      }
   }
}