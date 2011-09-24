package net.threesided.server.physics2d;

public class Physics2D {

	private RigidBody bodies[];
	
	private static final float ELASTICITY = 0.8f; //amount of ke transferred

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
				a.dx *= 1 - friction;
				a.dy *= 1 - friction;
				a.dx += a.xa;
				a.dy += a.ya;
				a.x += a.dx;//*((double) a.last/System.currentTimeMillis());
				a.y += a.dy;//*((double) a.last/System.currentTimeMillis());
				//a.last = System.currentTimeMillis();
			}

			for(int j = 1 + i; j < bodies.length; j++) {
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
			/*while (dist <= radius) {
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
			}*/
			//System.out.println("COLLIDE");
			float aInitialX = a.dx;
			float aInitialY = a.dy;
			a.dx = ((a.dx * (a.mass-b.mass) + (2 * b.mass * b.dx)) / (a.mass+b.mass)) / ELASTICITY;
			a.dy = ((a.dy * (a.mass-b.mass) + (2 * b.mass * b.dy)) / (a.mass+b.mass)) / ELASTICITY;
			b.dx = ((b.dx * (b.mass-a.mass) + (2 * a.mass * aInitialX)) / (a.mass+b.mass)) / ELASTICITY;
			b.dy = ((b.dy * (b.mass-a.mass) + (2 * a.mass * aInitialY)) / (a.mass+b.mass)) / ELASTICITY;
			return true;
		}
		return false;
	}

	double spring = 0.10;
	double friction = 0.028;
}
