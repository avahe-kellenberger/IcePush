package net.threesided.server.net.event;

import net.threesided.server.net.OPCode;

public interface NetworkEvent {
    /**
     * @return The OPCode associated with the event.
     */
    OPCode getOPCode();
}
