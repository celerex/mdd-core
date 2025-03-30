# Configuration example

I want to create a [configuration](https://en.wikipedia.org/wiki/Computer_configuration) file that is legible with text and stuff but also contains data.

## HTTP Server

The configuration for the http server.

http:
* port: 8080
* ssl: false
- listener:
	* class: com.example.Listener1
	* phase: early
- listener:
	* class: com.example.Listener2
	* phase: late
- listener:
	* class: com.example.Listener3
	* phase: early
	
## Mail server

Let's add a mail server as well!

mail:
* port: 8025
* starttls: true

## Basic properties

Some basic properties not related to anything in specific.

logLevel: INFO
development: false

This can be done as explicit keys as well and probably better.

* name: example-server
* clustered: true

## A list

And a list out of nowhere

- count: 1
- count: 2
- final: 3