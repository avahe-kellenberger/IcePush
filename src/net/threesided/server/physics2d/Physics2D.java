package net.threesided.server.physics2d;

public class Physics2D {

	//private RigidBody bodies[];
	
	private static final float ELASTICITY = 0.8f; //amount of ke transferred

	/*public Physics2D(RigidBody bodies[]) {
		this.bodies = bodies;
	}

	public Physics2D() {
		bodies = new RigidBody[100];
	}*/

	/*public void update() {
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
	}*/
	
	public void checkCollision(RigidBody bodyX, RigidBody bodyY) {
		double distX = bodyX.x - bodyY.x;
		double distY = bodyX.y - bodyY.y;
		double dist = Math.sqrt(distX*distX + distY*distY);
		double radius = bodyX.r + bodyY.r;
		if (dist <= radius) {
			doCollision(bodyX, bodyY);
			bodyX.x = bodyX.getPrevX();
			bodyX.y = bodyX.getPrevY();
			bodyY.x = bodyY.getPrevX();
			bodyY.y = bodyY.getPrevY();
		}
	}

	private void doCollision(RigidBody a, RigidBody b) {
		float aInitialX = a.dx;
		float aInitialY = a.dy;
		a.dx = ((a.dx * (a.mass-b.mass) + (2 * b.mass * b.dx)) / (a.mass+b.mass)) / ELASTICITY;
		a.dy = ((a.dy * (a.mass-b.mass) + (2 * b.mass * b.dy)) / (a.mass+b.mass)) / ELASTICITY;
		b.dx = ((b.dx * (b.mass-a.mass) + (2 * a.mass * aInitialX)) / (a.mass+b.mass)) / ELASTICITY;
		b.dy = ((b.dy * (b.mass-a.mass) + (2 * a.mass * aInitialY)) / (a.mass+b.mass)) / ELASTICITY;
	}

	double spring = 0.10;
	public static double FRICTION = 0.028;
}
