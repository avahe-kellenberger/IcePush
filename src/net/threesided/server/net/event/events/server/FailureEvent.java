package net.threesided.server.net.event.events.server;

import net.threesided.server.net.OPCode;
import net.threesided.server.net.WebSocketBuffer;
import net.threesided.server.net.event.ServerNetworkEvent;

public class FailureEvent extends ServerNetworkEvent {

    private final String message;

    /**
     * @param message The message of the event to send.
     */
    public FailureEvent(final String message) {
        this.message = message;
    }

    @Override
    public void write(final WebSocketBuffer buffer) {
        buffer.writeString(this.message);
    }

    @Override
    public OPCode getOPCode() {
        return OPCode.FAILURE;
    }

}
