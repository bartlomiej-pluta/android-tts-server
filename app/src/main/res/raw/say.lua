return {
    uri = "/say",
    method = Method.POST,
    queued = true,
    accepts = "application/json",
    consumer = function()
        tts.say("Hello, world!", "en")
    end
}