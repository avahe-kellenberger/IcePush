package net.threesided.server.net.event.events.server;

import net.threesided.server.net.event.OPCode;
import net.threesided.server.net.event.ServerNetworkEvent;
import net.threesided.shared.PacketBuffer;

/**
 * TODO: Note that the data type of `id` should be changed to `int`.
 */
public class LoginSuccessEvent extends ServerNetworkEvent {

    private final short playerID;

    /**
     * Event indicating a player's login request was successful.
     * @param playerID The player's server-assigned ID.
     */
    public LoginSuccessEvent(final PacketBuffer recipient, final short playerID) {
        super(recipient);
        this.playerID = playerID;
    }

    @Override
    public void writeDataToBuffer(final PacketBuffer buffer) {
        buffer.writeShort(this.playerID);
    }

    @Override
    public OPCode getOPCode() {
        return OPCode.SUCCESS;
    }

}
