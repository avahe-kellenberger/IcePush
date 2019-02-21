package net.threesided.server.physics2d;

import net.threesided.shared.*;

public class Projectile extends Circle {

    public int type;

    public Projectile(double radius) {
       super(radius);
    }

    public Projectile(double radius, double mass, Vector2D velocity) {
        super(radius, mass, velocity);
    }
}
