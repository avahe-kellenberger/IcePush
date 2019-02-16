package net.threesided.server.net.event;

import net.threesided.shared.PacketBuffer;

import java.util.Collection;
import java.util.Collections;

public abstract class ServerNetworkEvent implements NetworkEvent {

    protected final Collection<PacketBuffer> recipients;

    /**
     * TODO:
     * @param recipient
     */
    public ServerNetworkEvent(final PacketBuffer recipient) {
        this(Collections.singleton(recipient));
    }

    /**
     * TODO:
     * @param recipients
     */
    public ServerNetworkEvent(final Collection<PacketBuffer> recipients) {
        this.recipients = recipients;
    }

    /**
     * Writes the event's OPCode and data to the buffer of each recipient, described in the constructor.
     */
    public void writeToRecipients() {
        final byte opcode = this.getOPCode().getValue();
        this.recipients.forEach(buffer -> {
            buffer.beginPacket(opcode);
            this.writeDataToBuffer(buffer);
            buffer.endPacket();
            buffer.sync();
        });
    }

    /**
     * Writes the event data to a buffer.
     * @param buffer The buffer to write to.
     */
    protected abstract void writeDataToBuffer(final PacketBuffer buffer);

}