# What is it?
TTS Server is an Android-only application that provides a REST-based interface for on-board
TTS engine of your device. The application is aimed on home automation and allows you to give
a second life to your old smartphone, which can now act as a standalone, all-inclusive web TTS
provider for your speech-based home notification system. For the sake of elasticity,
the application is highly configurable via Lua scripts.

# For what?
Android is provided with a great *offline* TTS engine for almost every language on the
world.
If you care about high availability of the web TTS provider or you don't want your notifications
go through the Internet to the cloud providers, you can utilise your smartphone to done the job.

# How does it work?
TTS Server runs a service with HTTP server that exposes endpoints allowing you
to perform TTS task. As the Android implements its own process-killer, the application needs
to run the service *on the foreground* (so the notification is always shown)
keeping it alive and preventing it from being killed. Because of that, keep in mind that
the application might drain your battery, so it is recommended to use it *only*
on the devices being always connected to the power source.

## The elasticity
Actually, TTS Server is some kind of _framework_ for defining custom REST interface with Lua language.
Even though it comes with some predefined endpoints and configuration, you can freely
delete each endpoint, clear entire config file and implement everything by yourself from scratch.
Thanks to that you can force TTS Server to work as you want and to meet your requirements. 

# Defining REST interface
All endpoints are defined in Lua language so that you are able to freely modify them,
remove or even create new ones as you want.
All Lua scripts are stored in `<external_dir>/Android/data/com.bartlomiejpluta/files/endpoints`
directory. The endpoint definition is a simple Lua script, which returns a Lua table
containing some meta data about endpoints as well as the endpoint handler (called `consumer`) itself.
For example, the script for the `/tts.{ext}` endpoint is:
```lua
-- The 'return' is required - you can put anything you want in your script,
-- however only the returned table will determine the endpoint shape.
return {

    -- The path of endpoint.
    -- The '{ext}' enables you to capture the path parameter,
    -- so that you can use them in your code later.
    path = "/tts.{ext}",

    -- The supported HTTP method
    method = Method.GET,

    -- The endpoint handler (consumer) itself.
    -- As the argument it accepts the request table,
    -- which contains, among others:
    -- * the body of request (as string under the `body` key)
    -- * the path parameters (as table under the `path` key)
    -- * the query parameters (as table under the `query key`),
    --   where the query parameters means variables passed in the URL
    --   like "...?queryA=1&queryB=Hello"
    consumer = function(request)

        -- Take the "file extension" from URL and convert it to upper.
        -- The "wav" will be assigned if no extension is provided.
        local format = (request.path.ext or "wav"):upper()

        -- The AudioFormat as well as the Mime tables are global
        -- which contains the supported audio formats and MIME-Types string respectively.
        local audioFormat = AudioFormat[format]
        local mime = Mime[format]

        -- The heart of the script - performing TTS and saving the result to file of given format.
        -- The text will be spoken in language provided as a "lang" query
        -- or in English if no language is provided.
        local file = tts.sayToCache(request.query.phrase, request.query.lang or "en", audioFormat)

        -- Return the response - the `mime` key determines the `Content-Type` header,
        -- the `cached` determines whether to delete file immediately after response is sent,
        -- and the `data` file shapes the response's body.
        -- If it is of string type, it is being returned as text and can contain
        -- every kind of text based data - HTML, JSON, XML etc. (make sure to have
        -- corresponding `mime` field). Actually, in this case, it is the file returned
        -- from TTS engine. As long as this is of `java.io.File` type, the server
        -- automatically converts body to multipart message.
        return {
            mime = mime,
            cached = false,
            data = file
        }
    end
}
```

## Queued Endpoints
TTS Server also provides so called _Queued Endpoints_.
As long as the regular endpoints accept the response, process it and then return response,
the queued ones accept the response and immediately returns `202 Accepted`, and enqueue
the request so that it will be processed in single-thread queue as soon as requests precending
it have been processed. The `/say` endpoint is a great example of queued endpoint:
```lua
return {
    path = "/say",
    method = Method.POST,

    -- The `queued` flag tells the server to run the queue 
    -- and process all requests in it.
    queued = true,

    -- The `accepts` field is 1-to-1 mapping to "Accept" HTTP header
    accepts = Mime.JSON,
    
    -- In the terms of queued endpoints,
    -- the consumer function *is not* handling HTTP request itself nor returning response.
    -- Rather, it is executed for each enqueued request one by one.
    -- The method does not return anything, because it is running in dedicated thread
    -- and for the queued endpoints, the response is always `202 Accepted`.
    -- The consumer function in queued endpoints accepts the same request as the regular endpoints,
    -- and the function which returns remaining tasks in queue as the second argument. 
    consumer = function(request, queueSize)
    
        -- The `config` is table provided by global script,
        -- which enables you to have some global functions, configurations
        -- and preconditions that need to be met in order to complete the request.
        -- In this case, the config script defines the `silenceMode()` function,
        -- which determines if current time is in time window, 
        -- when no TTS actions should be performed.
        if(config.silenceMode()) then return end
        
        -- Decode JSON-encoded body to Lua table
        local body = json.decode(request.body)
        
        -- Perform TTS using device on-board speakers
        tts.say(body.text, body.language or "en")
    end
}
``` 

## Config
The `<external_dir>/Android/data/com.bartlomiejpluta/files/config/config.lua` file contains
a script which defines a global values that are accessible for each endpoint.
Similarly to endpoints script, the `config.lua` can contain anything, however it is obligated
to return single Lua table with arbitrary data. This table will be available to every endpoint
through `config` global (like the `config.silenceMode()` in the queued endpoint example).
The source code of `silenceMode()` function defined in `config.lua` is:
```lua
local silenceModeTimeRange = {  -- Silence mode is enabled:
    from = { 23, 00 },          -- from 23:00 each day
    to = { 07, 00 }             -- to 07:00 each day
}

-- The implementation of silenceMode() function
function silenceMode()
    local date = os.date("*t")

    local from = silenceModeTimeRange.from[1] * 60 + silenceModeTimeRange.from[2]
    local to = silenceModeTimeRange.to[1] * 60 + silenceModeTimeRange.to[2]
    local now = date.hour * 60 + date.min

    if (from <= to) then return from <= now and now <= to
    else return from <= now or now <= to end
end

-- some code has been cut for the sake of readability

-- The only thing required for config.lua is to return a single table (even empty).
-- In that case, the table contains only one function, which is `silenceMode()` defined above.
-- This function will be available through `config.silenceMode()` in each endpoint.
return {
    silenceMode = silenceMode
    -- some code has been cut for the sake of readability
}
```

Again, you are able to delete all predefined endpoints (`/say`, `/tts.{ext}` etc.) 
and write custom ones, as well as delete entire `config.lua` content and implement it by yourself.

## Global symbols
TTS Server provides some built-in symbols that are available to each endpoint 
as well as the config file:
* `Method` - a enum-like table which contains supported HTTP methods
* `Status` - a enum-like table which contains supported HTTP status codes
* `Mime` - a enum-like table which contains some common in the terms of TTS software MIME types
* `AudioFormat` - a enum-like table which contains audio formats supported by TTS engine
* `server` - a table containing data about running server instance. The table contains following fields:
  * `port` - the port of web server
  * `address` - the IP address of web server
  * `url` - the URL of server, composed of HTTP scheme, IP address and port
  * `getCachedFile(filename)` - the function which returns a finds the file in server cache by its filename and returns it as `java.io.File`
  * `debug(text)` - displays a popup on the application screen with given text
* `tts` - a table containing TTS-related methods. It contains following fields:
  * `say(text, lang)` - the function which performs TTS to device's on-board speakers.
  The `text` argument is the text to be spoken and the `lang` is the language as the _language tag_,
  which can be parsed by Java's `Locale.forLanguageTag()` method.
  * `sayToCache(text, lang, format)` - the function which performs TTS to file and returns it.
  The first two arguments are the same as of the `say()` function. 
  The `format` argument determines the format of the output file and it is highly recommended
  to use `AudioFormat` table's values here.
* `thread` - a table containing thread-related methods. So far it contains only one field:
  * `sleep(millis)` - hangs the thread execution for given amount of time (in milliseconds)
* `sonos` - a table containing Sonos devices-related methods. It contains following fields:
 * `discover()` - this function returns all found Sonos devices as Sonos objects
 * `of(address)` - this function return a Sonos device of given IP address as Sonos object
 
The Sonos object (note that we are talking about object, so each method should be invoked
with `:` operator) is coerction of `com.vmichalak.sonoscontroller.SonosDevice` Java object,
and each method is supported, so simply go [here](https://github.com/bartlomiej-pluta/sonos-controller/blob/master/src/main/java/com/vmichalak/sonoscontroller/SonosDevice.java) for a reference.


# Predefined endpoints
So far, the application is provided with four default endpoints, which are: 
`/say`, `/tts.{ext}`, `/sonos` and `/cache/{fileName}.{ext}`.

## The `/say` endpoint (`say.lua`)
```
POST /say
{
"text": "The text to be spoken",
"language": "en_US"
}
```
*Returns:* `202 Accepted` with empty body

The `/say` endpoint allows you to perform TTS task using device's speakers or the
external ones connected to it via jack, Bluetooth etc. For example if you have some 
old PC-speakers you are able to connect your device to them via line port and 
get a complete speech-based notification system.


## The `/tts.{ext}` endpoint
```
GET /tts.{wave,acc,mp3,m4a,wma,flac}?text=The+text+to+be+spoken&language=en_US
```
*Returns:* `200 OK` with proper audio file

This endpoint enables you to download an audio file file containing speech of the
provided text. The goal of this endpoint is to provide interface allowing you establishment
of the connection between the TTS Server and some other kind of already running TTS system,
which can invoke the HTTP request to your Android device and do something with returned
audio file. Note that all files but wav originates right from the wav file through FFmpeg
converter.

Example:
```
GET /tts.mp3?text=Hello+world&language=en_US
```

## The `/sonos` endpoint (`sonos.lua`)
```
POST /sonos
{
    "text": "The text to be spoken",
    "language": "en_US",
    "zone": "Living room",
    "volume": 60
}
```
*Returns:* `202 Accepted` meaning that the request has been queued and waits
to be completed.

The `/sonos` endpoint enables you to use your Sonos devices as a TTS speakers.
You need just to provide a text to be spoken and a desired zone name, where message is
supposed to be spoken. In the contrast to other endpoints, the `/sonos` endpoint
is non-blocking, which means your request is *accepted* and pushed at the end of
queue, where you are immediately given with response. The message waits in the queue to be
spoken protecting the Sonos device from messages race condition problem.


## The `/cache/{fileName}.{ext}` endpoint (`cache.lua`)
```
GET /cache/{fileName}.{wave,acc,mp3,m4a,wma,flac}
```
*Returns:* `200 OK` with proper audio file

Even though this endpoint is highly associated with `/sonos` endpoint, it simply returns
a file from server cache by its name and extension.
After hitting the `/sonos` endpoint, requested message is synthesized to wave file which is being
served through the `/cache/{fileName}.{ext}` endpoint and Sonos device is requested to
change its source stream URL to this file. The already generated files are stored in cache
directory so there is no need to resynthesize frequently-used message which reduces
the overall time needed to complete the request. You are still able to invalidate
this cache via application settings.
