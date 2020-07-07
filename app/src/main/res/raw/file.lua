return {
    path = "/tts.{ext}",
    method = Method.GET,
    consumer = function(request)
        local format = (request.path.ext or "wav"):upper()
        local audioFormat = AudioFormat[format]
        local mime = Mime[format]

        local file = tts.sayToCache(request.query.phrase, request.query.lang or "en", audioFormat)

        return {
            mime = mime,
            cached = false,
            data = file
        }
    end
}