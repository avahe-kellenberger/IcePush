package net.threesided.server.physics2d;

import net.threesided.shared.Vector2D;

public class Circle extends Entity {

    public final double radius;

    /** @param radius The circle's radius. */
    public Circle(final double radius) {
        this.radius = radius;
    }

    /**
     * @param circle The other circle.
     * @return The distance between the two circles; negative if overlapping.
     */
    public double distanceTo(final Circle circle) {
        return circle.getLocation().getDistance(this.getLocation()) - circle.radius - this.radius;
    }

    /**
     * @param point The point to check.
     * @return If the given point lies inside the circle.
     */
    public boolean containsPoint(final Vector2D point) {
        return this.getLocation().getDistance(point) < this.radius;
    }
}
