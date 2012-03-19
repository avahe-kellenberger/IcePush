package net.threesided.server.physics2d;

import net.threesided.server.Player;

public class Physics2D {

    private RigidBody bodies[];

    public Physics2D(RigidBody bodies[]) {
        this.bodies = bodies;
    }

    public Physics2D() {
        bodies = new RigidBody[100];
    }

    public void update() {
        RigidBody a = null, b = null;
        for (int i = 0; i < bodies.length; i++) {
            if ((a = bodies[i]) == null) continue;

            if(a.getClass().isAssignableFrom(Player.class) && ((Player) a).isDead)
                continue; // if it's a dead player, don't update

            if (a.movable) {
                // because screw proper physics, this is icepush!
                a.dx *= 1 - (FRICTION *a.mass);
                a.dy *= 1 - (FRICTION *a.mass);
                a.dx += a.xa;
                a.dy += a.ya;
                a.x += a.dx;
                a.y += a.dy;
            }

            for (int j = 1 + i; j < bodies.length; j++) {
                if ((b = bodies[j]) == null || (b == a)) continue;
                doCollision(a, b);
            }
        }
    }

    private void doCollision(RigidBody a, RigidBody b) {
        double distX = a.x - b.x;
        double distY = a.y - b.y;
        double d = Math.sqrt(distX * distX + distY * distY);
        double radius = a.r + b.r;

        if (d <= radius) {
            // someone's bad already intersecting fix code
            while (d <= radius) {
                if (a.x > b.x) {
                    a.x++;
                    b.x--;
                } else {
                    a.x--;
                    b.x++;
                }
                if (a.y > b.y) {
                    a.y++;
                    b.y--;
                } else {
                    a.y--;
                    b.y++;
                }
                distX = a.x - b.x;
                distY = a.y - b.y;
                d = (float) Math.sqrt(distX * distX + distY * distY);
            }

            // min trans dist
            double mtdX = distX * ((radius - d) / d);
            double mtdY = distY * ((radius - d) / d);

            // inverse mass
            double invMassA = 1 / a.mass;
            double invMassB = 1 / b.mass;

            // pos based off mass
            //double scaleX = mtdX * (invMassA / (invMassA + invMassB));
            //double scaleY = mtdY * (invMassA / (invMassA + invMassB));
            //a.x = a.x + scaleX;
            //a.y = a.y + scaleY;
            //b.x = b.x + scaleX;
            //b.y = b.y + scaleY;

            // impact velocity
            double impactVelocityX = a.dx - b.dx;
            double impactVelocityY = a.dy - b.dy;
            double mtdDelta = Math.sqrt(mtdX * mtdX + mtdY * mtdY);
            double mtdX2 = mtdX / mtdDelta;
            double mtdY2 = mtdY / mtdDelta;
            double vn = impactVelocityX * mtdX2 + impactVelocityY * mtdY2;

            // collision impulse
            double i = (-(ELASTICITY) * vn) / (invMassA + invMassB);
            double impulseX = mtdX * i;
            double impulseY = mtdY * i;
            double impulseAX = impulseX * invMassA;
            double impulseAY = impulseY * invMassA;
            double impulseBX = impulseX * invMassB;
            double impulseBY = impulseY * invMassB;

            // change velocity
            a.dx = a.dx + impulseAX;
            a.dy = a.dy + impulseAY;
            b.dx = b.dx - impulseBX;
            b.dy = b.dy - impulseBY;
        }
    }

    private static final double ELASTICITY = 1;
    private static final double FRICTION = 0.0046;
}