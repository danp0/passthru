# passthru

An application to pass thru messages from a client to a
server over TCP.

## Building

You will need [maven](https://maven.apache.org) to build this project.

## Usage

Pass thru message from a client to a server over TCP.

``` 
 java -jar target/passthru-1.0-SNAPSHOT.jar [-a <passthru address>] [-h] [-l <listen port>] [-p <passthru port>]

 -a,--address <passthru address>   the passthru address
 -h,--help                         show help
 -l,--listen <listen port>         listen on the server port
 -p,--port <passthru port>         the passthru port
```
The passthru displays a *$* prompt. You can enter one of the following commands:

* exit - exit the application.
* help - show help.
* rpts - repeatedly stop connection.
* rptc - cancel stopping connections.
* show - show the settings.
* stop - stop the connections.

## Examples

Listen on port 5000 and connect to port 5001.

```
$ java -jar target/passthru-1.0-SNAPSHOT.jar -a localhost -p 5001 -l 5000
19:27:43.259 [main] INFO  com.example.App - passthru...
19:27:43.270 [main] DEBUG com.example.App - listening: 5000
19:27:43.271 [main] DEBUG com.example.App - passthru: localhost:5001
$ 19:27:58.081 [pool-1-thread-1] DEBUG com.example.PassThruServer - new connection
19:28:02.759 [pool-1-thread-1] DEBUG com.example.PassThruClient - readHandler: 7
19:28:02.760 [pool-1-thread-1] DEBUG com.example.PassThruClient - C2S:
19:28:02.768 [pool-1-thread-1] DEBUG com.example.PassThruClient - 48 65 6C 6C 6F 0D 0A                             Hello..
19:28:02.823 [pool-1-thread-1] DEBUG com.example.PassThruClient - readHandler: 7
19:28:02.823 [pool-1-thread-1] DEBUG com.example.PassThruClient - S2C:
19:28:02.824 [pool-1-thread-1] DEBUG com.example.PassThruClient - 48 65 6C 6C 6F 0D 0A                             Hello..

$ help
Commands:
  exit - exit application
  help - show help
  rpts - repeat stop start
  rptc - repeat stop cancel
  show - show settings
  stop - stop clients
$ exit
19:28:20.335 [main] INFO  com.example.App - ...passthru
```

### Bugs
...

## License

Copyright © 2017 D. Pollind

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

