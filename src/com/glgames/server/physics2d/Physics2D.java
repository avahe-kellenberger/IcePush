package com.glgames.server.physics2d;

public class Physics2D {

	private RigidBody bodies[];
	
	private static final double PSUEDO_FRICTION_COEFFICIENT = 1.2;

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

			//float prevx = a.x;
			//float prevy = a.y;

			if(a.movable) {
				//double normalForce = a.mass * 9.81;
				//double frictionalForce = PSUEDO_FRICTION_COEFFICIENT * normalForce;
				//double frictionalVelocity = Math.sqrt((2*frictionalForce) / a.mass);
				//System.out.println(frictionalVelocity);
				a.dx *= (1 - friction);
				a.dy *= (1 - friction);
				a.dx += a.xa;
				a.dy += a.ya;
				a.x += a.dx*((double) a.last/System.currentTimeMillis());
				a.y += a.dy*((double) a.last/System.currentTimeMillis());
				a.last = System.currentTimeMillis();
			}

			for(int j = 0; j < bodies.length; j++) {
				if((b = bodies[j]) == null || (b == a)) continue;

				if(doCollision(a, b)) {
					//a.x = prevx;
					//a.y = prevy;
				}
			}
		}
	}

	private boolean doCollision(RigidBody a, RigidBody b) {
		double distX = a.x - b.x;
		double distY = a.y - b.y;
		double dist = Math.sqrt(distX * distX + distY * distY);
		double radius = a.r + b.r;
		if (dist <= radius) {
			while (dist <= radius) {
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
				dist = Math.sqrt(distX * distX + distY * distY);
			}
			//System.out.println("COLLIDE");
			/*double f4 = Math.atan2(distY, distX);
			double f5 = (b.x + Math.cos(f4) * b.r);
			double f6 = (b.y + Math.sin(f4) * b.r);
			double f7 = (f5 - a.x) * spring;
			double f8 = (f6 - a.y) * spring;
			a.dx -= f7;
			a.dy -= f8;
			b.dx += f7;
			b.dy += f8;*/
			float aInitialX = a.dx;
			float aInitialY = a.dy;
			a.dx = (a.dx * (a.mass-b.mass) + (2 * b.mass * b.dx)) / (a.mass+b.mass);
			a.dy = (a.dy * (a.mass-b.mass) + (2 * b.mass * b.dy)) / (a.mass+b.mass);
			b.dx = (b.dx * (b.mass-a.mass) + (2 * a.mass * aInitialX)) / (a.mass+b.mass);
			b.dy = (b.dy * (b.mass-a.mass) + (2 * a.mass * aInitialY)) / (a.mass+b.mass);
			return true;
		}
		return false;
	}

	double spring = 0.10;
	double friction = 0.018;
}