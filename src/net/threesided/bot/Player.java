package net.threesided.bot;


import net.threesided.shared.Vector2D;

public class Player {

    private String username;
    private Vector2D position;
    private boolean dead = true;

    public Player(String username) {
        this.username = username;
        this.position = new Vector2D();
    }

    public Vector2D getPosition() {
        return position;
    }

    public String getUsername() {
        return username;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public boolean getDead() {
        return dead;
    }
}
