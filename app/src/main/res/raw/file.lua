return {
    uri = "/{format}",
    method = Method.POST,
    accepts = Mime.JSON,
    consumer = function(request)
        local body = json.decode(request.body)
        local format = (request.params.format or "WAV"):upper()
        local audioFormat = AudioFormat[format]
        local mime = Mime[format]

        local file = tts.sayToFile(body.text, body.language or "en", audioFormat)

        return {
            mime = mime,
            data = file
        }
    end
}