package net.threesided.server.net.event;

import net.threesided.server.net.WebSocketBuffer;

public abstract class ClientNetworkEvent implements NetworkEvent {

    /**
     * Reads data sent by a client, from the buffer.
     * NOTE: This does NOT read the event's OPCode.
     *
     * @param buffer The buffer to read from.
     */
    public ClientNetworkEvent(final WebSocketBuffer buffer) {}

}
