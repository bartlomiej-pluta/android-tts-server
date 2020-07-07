return {
    path = "/say",
    method = Method.POST,
    queued = true,
    accepts = Mime.JSON,
    consumer = function(request)
        if(config.silenceMode()) then return end

        local body = json.decode(request.body)
        tts.say(body.text, body.language or "en")
    end
}