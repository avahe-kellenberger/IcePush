package net.threesided.server.net.event.events.server;

import net.threesided.server.net.WebSocketBuffer;
import net.threesided.server.net.event.OPCode;
import net.threesided.server.net.event.ServerNetworkEvent;

import java.util.Set;

public class NewPlayerEvent extends ServerNetworkEvent {

    private final byte id, type, lives;
    private final String username;

    /**
     * Creates a new event to notify all players of a new player logging into the game.
     * @param id The player's ID.
     * @param type The player's type.
     * @param username The player's username.
     * @param lives The number of lives the player has.
     */
    public NewPlayerEvent(final Set<WebSocketBuffer> recipients,
                          final byte id, final byte type,
                          final String username, final byte lives) {
        super(recipients);
        this.id = id;
        this.type = type;
        this.username = username;
        this.lives = lives;
    }

    @Override
    protected void writeDataToBuffer(final WebSocketBuffer buffer) {
        buffer.writeByte(this.id);
        buffer.writeByte(this.type);
        buffer.writeString(this.username);
        buffer.writeByte(this.lives);
    }

    @Override
    public OPCode getOPCode() {
        return OPCode.NEW_PLAYER;
    }

}
