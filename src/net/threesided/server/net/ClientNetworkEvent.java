package net.threesided.server.net;

public abstract class ClientNetworkEvent {

    /**
     * @param buffer The buffer from which to read the event.
     */
    public ClientNetworkEvent(final WebSocketBuffer buffer) {}

    /**
     * @return The OPCode associated with the event.
     */
    public abstract OPCode getOPCode();

}
