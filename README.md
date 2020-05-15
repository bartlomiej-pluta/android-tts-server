# What is it?
TTS Server is an Android-only application that provides a REST-based interface for on-board
TTS engine of your device. The application is aimed on home automation and allows you to give
a second life to your old smartphone, which can now act as a standalone, all-inclusive web TTS
provider for your speech-based home notification system.

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


# Consuming REST interface
So far, the application provides two endpoints: `/say`, `/wave`, `/sonos` and `/sonos/{fileName}`.
Each data-accepting endpoint works with JSON-based body, so each request carrying the data
requires a proper `Content-Type` header.

## The `/say` endpoint
```
POST /say
{
"text": "The text to be spoken",
"language": "en_US"
}
```
*Returns:* `200 OK` with empty body

The `/say` endpoint allows you to perform TTS task using device's speakers or the
external ones connected to it via jack, Bluetooth etc. For example if you have some 
old PC-speakers you are able to connect your device to them via line port and 
get a complete speech-based notification system.


## The `/wave` endpoint
```
POST /wave
{
"text": "The text to be spoken",
"language": "en_US"
}
```
*Returns:* `200 OK` with wave file (`Content-Type: audio/x-wav`)

The `/wave` endpoint enables you to download a wav file containing speech of the
provided text. The goal of this endpoint is to provide interface allowing you establishment
of the connection between the TTS Server and some other kind of already running TTS system,
which can invoke the HTTP request to your Android device and do something with returned
wav file.

## The `/sonos` endpoint
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


## The `/sonos/{fileName}` endpoint
```
GET /sonos/{fileName}
```
*Returns:* `200 OK` with wave file (`Content-Type: audio/x-wav`)

This endpoint is designed for serving synthesized wave files by `/sonos` endpoint
to Sonos devices and is not intended to be used directly by you. After hitting
the `/sonos` endpoint, requested message is synthesized to wave file which is being
served through the `/sonos/{fileName}` endpoint and Sonos device is requested to
change its source stream URL to this file. The already generated files are stored in cache
directory so there is no need to resynthesize frequently-used message which reduces
the overall time needed to complete the request. You are still able to invalidate
this cache via application settings.