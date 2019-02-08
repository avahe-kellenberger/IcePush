package net.threesided.server.net.event;

import net.threesided.server.net.WebSocketBuffer;

import java.util.Collections;
import java.util.Set;

public abstract class ServerNetworkEvent implements NetworkEvent {

    protected final Set<WebSocketBuffer> recipients;

    /**
     * TODO:
     * @param recipient
     */
    public ServerNetworkEvent(final WebSocketBuffer recipient) {
        this(Collections.singleton(recipient));
    }

    /**
     * TODO:
     * @param recipients
     */
    public ServerNetworkEvent(final Set<WebSocketBuffer> recipients) {
        this.recipients = recipients;
    }

    /**
     * Writes the event's OPCode and data to the buffer of each recipient, described in the constructor.
     */
    public void writeToRecipients() {
        final byte opcode = this.getOPCode().getValue();
        this.recipients.forEach(buffer -> {
            buffer.writeByte(opcode);
            this.writeDataToBuffer(buffer);
        });
    }

    /**
     * Writes the event data to a buffer.
     * @param buffer The buffer to write to.
     */
    protected abstract void writeDataToBuffer(final WebSocketBuffer buffer);

}