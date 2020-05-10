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
So far, the application provides two endpoints: `/say` and `/wave`.
Each endpoint works with JSON-based body, so each request requires a proper 
`Content-Type` header.

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
wav file. For example, take a look at
[my fork](https://github.com/bartlomiej-pluta/node-sonos-http-api) of great
[Node Sonos HTTP API](https://github.com/jishi/node-sonos-http-api).
I've already written a TTS plugin in my fork allowing me to connect the TTS Server and my
Sonos speakers right through the Node Sonos HTTP API, which performs the request
to the Android device and puts returned wav file on the Sonos speakers.
