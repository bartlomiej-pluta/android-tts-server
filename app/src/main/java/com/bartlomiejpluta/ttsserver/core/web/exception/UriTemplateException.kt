package com.bartlomiejpluta.ttsserver.core.web.exception

class UriTemplateException(message: String, val position: Int) : Exception(message)