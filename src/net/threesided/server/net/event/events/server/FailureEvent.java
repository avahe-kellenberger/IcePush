package net.threesided.server.net.event.events.server;

import net.threesided.server.net.event.OPCode;
import net.threesided.server.net.WebSocketBuffer;
import net.threesided.server.net.event.ServerNetworkEvent;
import net.threesided.shared.PacketBuffer;

public class FailureEvent extends ServerNetworkEvent {

    private final String message;

    /**
     * @param message The message of the event to send.
     */
    public FailureEvent(final PacketBuffer recipient, final String message) {
        super(recipient);
        this.message = message;
    }

    @Override
    public void writeDataToBuffer(final PacketBuffer buffer) {
        buffer.writeString(this.message);
    }

    @Override
    public OPCode getOPCode() {
        return OPCode.FAILURE;
    }

}
