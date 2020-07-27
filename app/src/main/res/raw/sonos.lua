local snapshot

function prepareTTSFile(phrase, language)
    log.info("Saying to cache file (lang: " .. language .. ")...")
    local file = tts.sayToCache(phrase, language, AudioFormat.MP3)
    return string.format("%s/cache/%s", server.url, file:getName())
end

function updateSnapshotIfFirst(device)
    if(snapshot == nil) then
        log.info("Dumping the Sonos state snapshot...")
        snapshot = device:snapshot()
    end
end

function announce(device, data, url)
    device:stop()
    device:setVolume(data.volume)
    log.info("Announcing on '" .. data.zone .. "' zone...")
    device:playUri(url, "")
    while(device:getPlayState():name() ~= "STOPPED") do
        thread.sleep(500)
    end
    log.info("Announcement is complete")
end

function restoreSnapshotIfLast(queueLength)
    if(queueLength() == 0) then
        if(snapshot ~= nil) then
            log.info("Restoring the Sonos state snapshot...")
            snapshot:restore()
            snapshot = nil
        end
    end
end

return {
    path = "/sonos",
    method = Method.POST,
    accepts = Mime.JSON,
    queued = true,
    consumer = function(request, queueLength)
        if(config.silenceMode()) then return end
        local body = json.decode(request.body)
        local zone = config.sonosDevices[body.zone]

        local url = prepareTTSFile(body.text, body.language or "en")
        updateSnapshotIfFirst(zone)
        announce(zone, body, url)
        restoreSnapshotIfLast(queueLength)
    end
}