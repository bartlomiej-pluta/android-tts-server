local silenceModeTimeRange = {
    from = { 23, 00 },
    to = { 07, 00 }
}

function silenceMode()
    local date = os.date("*t")

    local from = silenceModeTimeRange.from[1] * 60 + silenceModeTimeRange.from[2]
    local to = silenceModeTimeRange.to[1] * 60 + silenceModeTimeRange.to[2]
    local now = date.hour * 60 + date.min

    if (from <= to) then return from <= now and now <= to
    else return from <= now or now <= to end
end

function discoverSonosDevices()
    local output = {}
    local devices = sonos.discover()

    log.info("Discovering Sonos devices...")
    for _, device in ipairs(devices) do
        local name = device:getZoneGroupState():getName()
        output[name] = device
        log.info("Discovered '" .. name .. "' as " .. tostring(device))
    end

    return output
end

return {
    silenceMode = silenceMode,
    sonosDevices = discoverSonosDevices()
}
