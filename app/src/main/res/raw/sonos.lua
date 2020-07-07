local snapshot

function prepareTTSFile(phrase, language)
    local file = tts.sayToFile(phrase, language, AudioFormat.MP3)
    return string.format("%s/cache/%s", server.url, file:getName())
end

function updateSnapshotIfFirst(device)
    if(snapshot == nil) then
        snapshot = device:snapshot()
    end
end

function announce(device, data, url)
    device:stop()
    device:setVolume(data.volume)
    device:playUri(url, "")
    while(device:getPlayState():name() ~= "STOPPED") do
        thread.sleep(500)
    end
end

function restoreSnapshotIfLast(queueLength)
    if(queueLength() == 0) then
        if(snapshot ~= nil) then
            snapshot:restore()
            snapshot = nil
        end
    end
end

return {
    uri = "/sonos",
    method = Method.POST,
    accepts = Mime.JSON,
    queued = true,
    consumer = function(request, queueLength)
        local body = json.decode(request.body)
        local zone = config.sonosDevices[body.zone]

        local url = prepareTTSFile(body.text, body.language or "en")
        updateSnapshotIfFirst(zone)
        announce(zone, body, url)
        restoreSnapshotIfLast(queueLength)
    end
}