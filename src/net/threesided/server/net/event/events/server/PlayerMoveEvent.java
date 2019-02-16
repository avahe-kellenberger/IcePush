package net.threesided.server.net.event.events.server;

import net.threesided.server.Player;
import net.threesided.server.net.event.OPCode;
import net.threesided.server.net.event.ServerNetworkEvent;
import net.threesided.shared.PacketBuffer;
import net.threesided.shared.Vector2D;

import java.util.Collection;

public class PlayerMoveEvent extends ServerNetworkEvent {

    private final short playerID, xLoc, yLoc;

    /**
     * @param player The player which has moved.
     */
    public PlayerMoveEvent(final Collection<PacketBuffer> recipients, final Player player) {
        super(recipients);
        this.playerID = player.getID();
        final Vector2D loc = player.getLocation();
        this.xLoc = (short) loc.x;
        this.yLoc = (short) loc.y;
    }

    @Override
    protected void writeDataToBuffer(final PacketBuffer buffer) {
        buffer.writeShort(this.playerID);
        buffer.writeShort(this.xLoc);
        buffer.writeShort(this.yLoc);
    }

    @Override
    public OPCode getOPCode() {
        return OPCode.PLAYER_MOVE;
    }

}
