package net.threesided.server.physics2d;

import net.threesided.shared.Vector2D;

public class Physics2D {

    private Circle[] circles;

    public Physics2D(Circle[] bodies) {
        this.circles = bodies;
    }

    public void update() {
        Circle a, b;
        for (int i = 0; i < circles.length; i++) {
            if ((a = circles[i]) == null) continue;

            if (a.movable) {
                // because screw proper physics, this is icepush!
                a.velocity.multiply(1 - FRICTION);
                a.velocity.add(a.acceleration);
                a.position.add(a.velocity);
            }

            for (int j = 1 + i; j < circles.length; j++) {
                if ((b = circles[j]) == null || (b == a)) continue;
                doCollision(a, b);
            }
        }
    }

    private void doCollision(Circle a, Circle b) {
        Vector2D delta = new Vector2D(a.position).subtract(b.position);
        double d = delta.getLength();
        double r = a.radius + b.radius;

        if (d > r) return;
        Vector2D mtd = new Vector2D(delta).multiply((r - d) / d);

        double invMassA = 1 / a.mass;
        double invMassB = 1 / b.mass;
        double invMass = invMassA + invMassB;

        // push the balls proportionate to mass
        a.position.add(new Vector2D(mtd).multiply(invMassA / invMass));
        b.position.subtract(new Vector2D(mtd).multiply(invMassB / invMass));

        Vector2D impactVelocity = new Vector2D(a.velocity).subtract(b.velocity);
        double normalVelocity = impactVelocity.dot(mtd.normalize());

        if (normalVelocity > 0) return; // already moving away

        double i = (-((2.0 + SPRING) * normalVelocity) / invMass);
        Vector2D impulse = new Vector2D(mtd).multiply(i);

        a.velocity.add(new Vector2D(impulse).multiply(invMassA));
        b.velocity.subtract(new Vector2D(impulse).multiply(invMassB));
    }

    private static final double SPRING = 0;
    private static final double FRICTION = 0.0059;
}
