package net.threesided.server.physics2d;

import net.threesided.shared.Vector2D;

public class RigidBody {

    public Vector2D position = new Vector2D();
    protected Vector2D velocity = new Vector2D();
    protected Vector2D acceleration = new Vector2D();

    boolean movable = true;
    protected double mass;
    private int savedX, savedY;

    /**
     * @return If the object has moved since this method's last invocation.
     */
    public boolean hasMoved() {
        final int x = (int) this.position.getX();
        final int y = (int) this.position.getY();
        final boolean result = this.savedX != x || this.savedY != y;
        this.savedX = x;
        this.savedY = y;
        return result;
    }

}