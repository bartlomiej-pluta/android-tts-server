return {
    path = "/tts.{ext}",
    method = Method.GET,
    consumer = function(request)
        local format = (request.path.ext or "wav"):upper()
        local audioFormat = AudioFormat[format]
        local mime = Mime[format]
        local language = request.query.lang or "en"

        log.info("Saying to " .. format .. " file (lang: " .. language .. ")...")
        local file = tts.sayToCache(request.query.phrase, language, audioFormat)

        return {
            mime = mime,
            cached = false,
            data = file
        }
    end
}