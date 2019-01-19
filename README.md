# IcePush

## Server Configuration

Copy `server.config-copy` into the directory from which the server will run, and rename it to be `server.config`.

`server.config` must have the following attributes:

```
bind-port:${SERVER_PORT_NUMBER}
irc-server:${SERVER_ADDRESS}
irc-channel:${#CHANNEL}
irc-port:${IRC_PORT_NUMBER}
irc-nick:${IRC_NICK}
```

Default/example values can be found in `server.config-copy`.


## Server Deployment

Build the `.jar` file with `net.threesided.server.Server` as the main class.

Ensure that `server.config` is in the same directory as `IcePush.jar`.

Run the server: `java -jar IcePush.jar`

The server takes an option flag `true` if it is to only be ran locally.
This will also prevent the server from connecting to the IRC.