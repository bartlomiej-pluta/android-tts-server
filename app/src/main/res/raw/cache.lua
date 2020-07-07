return {
    uri = "/cache/{filename}.{ext}",
    method = Method.GET,
    consumer = function(request)
        local filename = string.format("%s.%s", request.path.filename, request.path.ext)
        local file = cache.file(filename)
        local mime = Mime[request.path.ext:upper()]

        return {
            mime = mime,
            cached = true,
            data = file
        }
    end
}