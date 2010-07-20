package com.glgames.server.physics2d;

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
		for(int i = 0; i < bodies.length; i++) {
			if((a = bodies[i]) == null) continue;

			float prevx = a.x;
			float prevy = a.y;

			if(a.movable) {
				a.dx *= friction;
				a.dy *= friction;

				a.dx += a.xa;
				a.dy += a.ya;

				a.x += a.dx;
				a.y += a.dy;
			}

			for(int j = 0; j < bodies.length; j++) {
				if((b = bodies[j]) == null || (b == a)) continue;

				if(doCollision(a, b)) {
					a.x = prevx;
					a.y = prevy;
				}
			}
		}
	}

	private boolean doCollision(RigidBody a, RigidBody b) {
		double distX = a.x - b.x;
		double distY = a.y - b.y;
		double dist = Math.sqrt(distX * distX + distY * distY);
		double radius = a.r + b.r;
		if (dist < radius) {
			double f4 = Math.atan2(distY, distX);
			double f5 = (b.x + Math.cos(f4) * b.r);
			double f6 = (b.y + Math.sin(f4) * b.r);
			double f7 = (f5 - a.x) * spring;
			double f8 = (f6 - a.y) * spring;
			b.dx -= f7;
			b.dy -= f8;
			a.dx += f7;
			a.dy += f8;
			return true;
		}
		return false;
	}

	double spring = 1.0;
	double friction = 0.96;
}