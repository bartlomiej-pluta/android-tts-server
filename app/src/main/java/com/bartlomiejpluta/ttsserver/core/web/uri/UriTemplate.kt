package com.bartlomiejpluta.ttsserver.core.web.uri

import com.bartlomiejpluta.ttsserver.core.web.exception.UriTemplateException
import java.util.regex.Pattern

class UriTemplate private constructor(val template: String) {
   private val variables = mutableListOf<String>()
   private var pattern: Pattern

   init {
      val patternBuilder = StringBuilder()
      var variableBuilder: StringBuilder? = null
      var isVariable = false

      template.forEachIndexed { index, char ->
         when {
            char == '{' -> {
               if (isVariable) {
                  error("Templates cannot be nested", index + 1)
               }
               isVariable = true
               variableBuilder = StringBuilder()
               patternBuilder.append("(\\w+)")
            }

            char == '}' -> {
               isVariable = false
               variables.add(variableBuilder.toString())
            }

            isVariable -> char.takeIf { it.isLetter() }
               ?.let { variableBuilder?.append(it) }
               ?: error("Only letters are allowed as template", index + 1)

            else -> patternBuilder.append(Pattern.quote(char.toString()))
         }
      }

      if (isVariable) {
         error("Unclosed template found", patternBuilder.length)
      }

      pattern = Pattern.compile("^${patternBuilder.toString()}\$")
   }

   private fun error(message: String, position: Int): Unit =
      throw UriTemplateException(message, position)


   fun match(url: String): MatchingResult {
      val matcher = pattern.matcher(url)

      if (matcher.find()) {
         val matchedVariables = IntRange(0, matcher.groupCount() - 1)
            .map { variables[it] to matcher.group(it + 1)!! }
            .toMap()

         return MatchingResult(true, matchedVariables)
      }

      return MatchingResult(false)
   }

   data class MatchingResult(val matched: Boolean, val variables: Map<String, String> = emptyMap())

   companion object {
      fun parse(uriTemplate: String): UriTemplate {
         return UriTemplate(uriTemplate)
      }
   }
}