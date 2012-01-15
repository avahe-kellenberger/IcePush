package net.threesided.server.physics2d;

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

			float ar = a.r;
			float br = b.r;

			float dX = a.x - b.x + (ar - br);
			float dY = a.y - b.y + (ar - br);

			double bounceAngleA = computeBounceAngle(computeAngle(a.dx, a.dy), computeAngle(dX, dY));
			double bounceAngleB = computeBounceAngle(computeAngle(b.dx, b.dy), computeAngle(dX, dY));

			float aInitialX = a.dx;
			float aInitialY = a.dy;

			a.dx = ((a.dx * (a.mass-b.mass) + (2 * b.mass * b.dx)) / (a.mass+b.mass)) / a.elasticity;
			a.dy = ((a.dy * (a.mass-b.mass) + (2 * b.mass * b.dy)) / (a.mass+b.mass)) / a.elasticity;
			b.dx = ((b.dx * (b.mass-a.mass) + (2 * a.mass * aInitialX)) / (a.mass+b.mass)) / b.elasticity;
			b.dy = ((b.dy * (b.mass-a.mass) + (2 * a.mass * aInitialY)) / (a.mass+b.mass)) / b.elasticity;

			double aRad = Math.sqrt(a.dx*a.dx + a.dy*a.dy);
			double bRad = Math.sqrt(b.dx*b.dx + b.dy*b.dy);

			a.dx = (float)(aRad*Math.cos(bounceAngleA));
			a.dy = (float)(aRad*Math.sin(bounceAngleA));

			b.dx = (float)(bRad*Math.cos(bounceAngleB));
			b.dy = (float)(bRad*Math.sin(bounceAngleB));

			return true;
		}
		return false;
	}

	/*private float computeBounceAngle(float dX, float dY, float xDir, float yDir) {
		float vecAngle = computeAngle(xDir, yDir);
		float linePerp = Math.PI/2 + computeAngle(dX, dY);
		return Math.PI + (2*linePerp - vecAngle);
	}*/

	private double computeBounceAngle(double moveAngle, double surfaceAngle) {
		return 2*surfaceAngle - moveAngle;
	}

	private double computeAngle(double deltaX, double deltaY){
		if(deltaX == 0)
			return deltaY < 0? Math.PI/2 : -Math.PI/2;
		double result = Math.atan(deltaY/deltaX);
		if(deltaX > 0) result += Math.PI;
		return result;
	}

	double spring = 0.10;
	double friction = 0.028;
}
