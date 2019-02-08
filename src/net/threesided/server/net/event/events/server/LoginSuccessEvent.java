package net.threesided.server.net.event.events.server;

import net.threesided.server.net.event.OPCode;
import net.threesided.server.net.WebSocketBuffer;
import net.threesided.server.net.event.ServerNetworkEvent;

public class LoginSuccessEvent extends ServerNetworkEvent {

    private final byte playerID;

    /**
     * Event indicating a player's login request was successful.
     * @param playerID The player's server-assigned ID.
     */
    public LoginSuccessEvent(final WebSocketBuffer recipient, final byte playerID) {
        super(recipient);
        this.playerID = playerID;
    }

    @Override
    public void writeDataToBuffer(final WebSocketBuffer buffer) {
        buffer.writeByte(this.playerID);
    }

    @Override
    public OPCode getOPCode() {
        return OPCode.SUCCESS;
    }

}
