package net.threesided.server.net.events;

import net.threesided.server.net.ClientNetworkEvent;
import net.threesided.server.net.OPCode;
import net.threesided.server.net.WebSocketBuffer;

public class LoginEvent extends ClientNetworkEvent {

    public final int clientVersion;
    public final String playerName;

    /**
     * @param buffer The buffer from which to read the event.
     */
    public LoginEvent(final WebSocketBuffer buffer) {
        super(buffer);
        this.clientVersion = buffer.readByte();
        this.playerName = buffer.readString();
    }

    @Override
    public OPCode getOPCode() {
        return OPCode.LOGIN;
    }

}
