package net.threesided.server.physics2d;

import net.threesided.server.Player;
import net.threesided.shared.Vector2D;

public class Physics2D {

    private RigidBody bodies[];

    public Physics2D(RigidBody bodies[]) {
        this.bodies = bodies;
    }

    public void update() {
        RigidBody a, b;
        for (int i = 0; i < bodies.length; i++) {
            if ((a = bodies[i]) == null) continue;

            if (a.getClass().isAssignableFrom(Player.class) && ((Player) a).isDead)
                continue; // if it's a dead player, don't update

            if (a.movable) {
                // because screw proper physics, this is icepush!
                a.velocity.multiply(1 - (FRICTION * a.mass));
                a.velocity.add(a.acceleration);
                a.position.add(a.velocity);
            }

            for (int j = 1 + i; j < bodies.length; j++) {
                if ((b = bodies[j]) == null || (b == a)) continue;
                if (b.getClass().isAssignableFrom(Player.class) && ((Player) b).isDead)
                    continue; // if it's a dead player, don't attempt to collide
                doCollision(a, b);
            }
        }
    }

    private void doCollision(RigidBody a, RigidBody b) {
        Vector2D delta = new Vector2D(a.position).subtract(b.position);
        double d = delta.getLength();
        double r = a.r + b.r;
       
        if (d > r) return;
        /*while (d <= r) {
            if (a.position.getX() > b.position.getX()) {
                a.position.setX(a.position.getX() + 1);
                b.position.setX(b.position.getX() - 1);
            } else {
                a.position.setX(a.position.getX() - 1);
                b.position.setX(b.position.getX() + 1);
            }
            if (a.position.getY() > b.position.getY()) {
                a.position.setY(a.position.getY() + 1);
                b.position.setY(b.position.getY() - 1);
            } else {
                a.position.setY(a.position.getY() - 1);
                b.position.setY(b.position.getY() + 1);
            }
            delta = new Vector2D(a.position).subtract(b.position);
            d = delta.getLength();
        }*/

        Vector2D mtd = new Vector2D(delta).multiply((r - d) / d);

        double invMassA = 1 / a.mass;
        double invMassB = 1 / b.mass;
        double invMass = invMassA + invMassB;

        // push the balls proportionate to mass
        a.position.add(new Vector2D(mtd).multiply(invMassA / invMass));
        b.position.subtract(new Vector2D(mtd).multiply(invMassB / invMass));

        Vector2D impactVelocity = new Vector2D(a.velocity).subtract(b.velocity);
        double normalVelocity = impactVelocity.dot(mtd.normalize());

        double i = (-((1.0 + ELASTICITY) * normalVelocity) / invMass);
        Vector2D impulse = new Vector2D(mtd).multiply(i);

        a.velocity.add(new Vector2D(impulse).multiply(invMassA));
        b.velocity.subtract(new Vector2D(impulse).multiply(invMassB));
    }

    private static final double ELASTICITY = 1.5;
    private static final double FRICTION = 0.0046;
}