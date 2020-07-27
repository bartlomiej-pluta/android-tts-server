return {
    path = "/say",
    method = Method.POST,
    queued = true,
    accepts = Mime.JSON,
    consumer = function(request)
        if(config.silenceMode()) then return end
        local body = json.decode(request.body)
        local language = body.language or "en"

        log.info("Saying (lang: " .. language .. ")...")
        tts.say(body.text, language)
    end
}