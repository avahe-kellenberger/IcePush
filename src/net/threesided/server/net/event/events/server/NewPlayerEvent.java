package net.threesided.server.net.event.events.server;

import net.threesided.server.Player;
import net.threesided.server.net.event.OPCode;
import net.threesided.server.net.event.ServerNetworkEvent;
import net.threesided.shared.PacketBuffer;

import java.util.Collection;

public class NewPlayerEvent extends ServerNetworkEvent {

    private final short id;
    private final byte type, lives;
    private final String username;

    /**
     * Creates a new event to notify all players of a new player logging into the game.
     * @param player The new player.
     */
    public NewPlayerEvent(final Collection<PacketBuffer> recipients, final Player player) {
        this(recipients, player.getID(), player.getType().getID(), player.getUsername(), player.getLives());
    }

    /**
     * Creates a new event to notify all players of a new player logging into the game.
     * @param id The player's ID.
     * @param type The player's type.
     * @param username The player's username.
     * @param lives The number of lives the player has.
     */
    public NewPlayerEvent(final Collection<PacketBuffer> recipients,
                          final short id, final byte type,
                          final String username, final byte lives) {
        super(recipients);
        this.id = id;
        this.type = type;
        this.username = username;
        this.lives = lives;
    }

    @Override
    protected void writeDataToBuffer(final PacketBuffer buffer) {
        buffer.writeShort(this.id);
        buffer.writeByte(this.type);
        buffer.writeString(this.username);
        buffer.writeByte(this.lives);
    }

    @Override
    public OPCode getOPCode() {
        return OPCode.NEW_PLAYER;
    }

}
