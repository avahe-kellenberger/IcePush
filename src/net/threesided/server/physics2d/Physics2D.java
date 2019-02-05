package net.threesided.server.physics2d;

import net.threesided.shared.Vector2D;

public class Physics2D {

    private static final double SPRING = 0;
    private static final double FRICTION = 0.0059;

    private Circle[] circles;

    public Physics2D(Circle[] bodies) {
        this.circles = bodies;
    }

    public void update(final double elapsed) {
        Circle circleA, circleB;
        for (int i = 0; i < this.circles.length; i++) {
            if ((circleA = this.circles[i]) == null) {
                continue;
            }

            if (circleA.isMovable()) {
                final Vector2D newVelocity = circleA.getVelocity().add(circleA.getAcceleration());
                circleA.setVelocity(newVelocity.multiply(1 - Physics2D.FRICTION).multiply(elapsed));
                circleA.translate(newVelocity.multiply(elapsed));
            }

            for (int j = 1 + i; j < this.circles.length; j++) {
                if ((circleB = this.circles[j]) == null || (circleB == circleA)) {
                    continue;
                }
                this.resolveCollision(circleA, circleB);
            }
        }
    }

    private void resolveCollision(final Circle circleA, final Circle circleB) {
        final Vector2D delta = circleA.getLocation().subtract(circleB.getLocation());
        final double d = delta.getMagnitude();
        final double r = circleA.radius + circleB.radius;

        if (d > r) {
            return;
        }

        final Vector2D mtd = delta.multiply((r - d) / d);

        final double invMassA = circleA.isMovable() ? 1 / circleA.getMass() : 0;
        final double invMassB = circleB.isMovable() ? 1 / circleB.getMass() : 0;
        final double invMass = invMassA + invMassB;
        if (invMass == 0) {
            return;
        }

        // Push the balls proportionate to mass
        final Vector2D pushbackVector = mtd.multiply(invMassA / invMass);
        circleA.translate(pushbackVector);
        circleB.translate(pushbackVector.negate());

        final Vector2D velCircleA = circleA.getVelocity();
        final Vector2D impactVelocity = velCircleA.subtract(circleB.getVelocity());
        final double normalVelocity = impactVelocity.dot(mtd.normalize());

        if (normalVelocity > 0) {
            // Objects already moving away from each other.
            return;
        }

        double i = (-((2.0 + Physics2D.SPRING) * normalVelocity) / invMass);
        final Vector2D impulse = mtd.multiply(i);

        circleA.setVelocity(velCircleA.add(impulse.multiply(invMassA)));
        circleB.setVelocity(circleB.getVelocity().subtract(impulse.multiply(invMassB)));
    }

}
