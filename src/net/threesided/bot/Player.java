package net.threesided.bot;

import net.threesided.server.physics2d.Entity;

public class Player extends Entity {

    private final String username;
    private boolean isDead = true;

    public Player(final String username) {
        super();
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setDead(final boolean isDead) {
        this.isDead = isDead;
    }

    public boolean isDead() {
        return this.isDead;
    }

}
