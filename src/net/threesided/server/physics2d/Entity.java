package net.threesided.server.physics2d;

import net.threesided.shared.Vector2D;

/**
 *
 */
public class Entity {

    private Vector2D location, cachedLocation, velocity, acceleration;
    private boolean movable = true;
    private double mass;

    /**
     *
     */
    public Entity() {
        this.location = Vector2D.ZERO;
        this.cachedLocation = Vector2D.ZERO;
        this.velocity = Vector2D.ZERO;
        this.acceleration = Vector2D.ZERO;
    }

    /**
     * @return The object's location.
     */
    public Vector2D getLocation() {
        return this.location;
    }

    /**
     * @param location The object's new location.
     */
    public void setLocation(final Vector2D location) {
        this.location = location;
    }

    /**
     * Moves the object by the given value.
     * @param deltaLocation The value in which to move the object's location.
     */
    public void translate(final Vector2D deltaLocation) {
        this.setLocation(this.location.add(deltaLocation));
    }

    /**
     * @return The object's velocity.
     */
    public Vector2D getVelocity() {
        return this.velocity;
    }

    /**
     * @param velocity The object's new velocity.
     */
    public void setVelocity(final Vector2D velocity) {
        this.velocity = velocity;
    }

    /**
     * @return The object's acceleration.
     */
    public Vector2D getAcceleration() {
        return this.acceleration;
    }

    /**
     * @param acceleration The new acceleration.
     */
    public void setAcceleration(final Vector2D acceleration) {
        this.acceleration = acceleration;
    }

    /**
     * @return The mass of the object.
     */
    public double getMass() {
        return this.mass;
    }

    /**
     * Sets the object's mass.
     * @param mass The new mass of the object.
     */
    public void setMass(final double mass) {
        this.mass = mass;
    }

    /**
     * @return If the object is able to be moved.
     */
    public boolean isMovable() {
        return this.movable;
    }

    /**
     * @param movable If the object should be movable.
     * @return If the movable state of the object was changed.
     */
    public boolean setMovable(final boolean movable) {
        if (this.movable == movable) {
            return false;
        }
        this.movable = movable;
        return true;
    }

    /**
     * @return If the object has moved since this method's last invocation.
     */
    public boolean hasMoved() {
        if (!this.location.equals(this.cachedLocation)) {
            this.cachedLocation = this.location;
            return true;
        }
        return false;
    }

}
