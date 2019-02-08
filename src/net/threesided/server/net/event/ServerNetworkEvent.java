package net.threesided.server.net.event;

import net.threesided.server.net.WebSocketBuffer;

public abstract class ServerNetworkEvent implements NetworkEvent {

    /**
     * Writes the event data to the buffer.
     * NOTE: This does NOT include the event's OPCode.
     *
     * @param buffer The buffer to write to.
     */
    public abstract void write(final WebSocketBuffer buffer);

}