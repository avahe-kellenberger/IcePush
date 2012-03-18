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
				doCollision(a, b);
				//if(doCollision(a, b)) {
					//a.x = prevx;
					//a.y = prevy;
				//}
			}
		}
	}

        private void doCollision(RigidBody a, RigidBody b) {
                float distX = a.x - b.x;
                float distY = a.y - b.y;
                float d = (float) Math.sqrt(distX * distX + distY * distY);
                float radius = a.r + b.r;
               
                if (d <= radius) {
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
                d = (float)Math.sqrt(distX * distX + distY * distY);
            }
               
                        // min trans dist
                        float mtdX = distX*((radius-d)/d);
                        float mtdY = distY*((radius-d)/d);
               
                        // inverse mass
                        float invMassA = 1/a.mass;
                        float invMassB = 1/b.mass;
                       
                        // pos based off mass
                        float scaleX = mtdX*(invMassA/(invMassA+invMassB));
                        float scaleY = mtdY*(invMassA/(invMassA+invMassB));
                        a.x = a.x+scaleX;
                        a.y = a.y+scaleY;
                        b.x = b.x+scaleX;
                        b.y = b.y+scaleY;
                       
                        // impact velocity
                        float impactVelocityX = a.dx-b.dx;
                        float impactVelocityY = a.dy-b.dy;
                        float mtdDelta = (float) Math.sqrt(mtdX * mtdX + mtdY * mtdY);
                        float mtdX2 = mtdX/mtdDelta;
                        float mtdY2 = mtdY/mtdDelta;
                        float vn = dotProduct(impactVelocityX, mtdX2, impactVelocityY, mtdY2);
                       
                        // collision impulse
                        float i = (-(restitution)*vn)/(invMassA+invMassB);
                        float impulseX = mtdX*i;
                        float impulseY = mtdY*i;
                        float impulseAX = impulseX*invMassA;
                        float impulseAY = impulseY*invMassA;
                        float impulseBX = impulseX*invMassB;
                        float impulseBY = impulseY*invMassB;
                       
                        // change velocity
                        a.dx = a.dx+impulseAX;
                        a.dy = a.dy+impulseAY;
                        b.dx = b.dx-impulseBX;
                        b.dy = b.dy-impulseBY;
                       
                     //   return true;
                }
              //  return false;
        }
	
	public float dotProduct(float x1, float x2, float y1, float y2) {
		return (float) (x1*x2+y1*y2);
	}

	float restitution = 1.15f; // aka collision friction
	double friction = 0.028;
}