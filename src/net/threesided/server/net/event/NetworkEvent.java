package net.threesided.server.net.event;

public interface NetworkEvent {
    /**
     * @return The OPCode associated with the event.
     */
    OPCode getOPCode();
}
