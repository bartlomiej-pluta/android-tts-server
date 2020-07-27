return {
    path = "/cache/{filename}.{ext}",
    method = Method.GET,
    consumer = function(request)
        local filename = string.format("%s.%s", request.path.filename, request.path.ext)
        local file = server.getCachedFile(filename)
        local format = request.path.ext:upper()
        local mime = Mime[format]

        log.info("Returning the " .. format .. " file from cache...")

        return {
            mime = mime,
            cached = true,
            data = file
        }
    end
}