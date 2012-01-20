package net.threesided.server.physics2d;

public class RigidBody {

	public final static int RBTYPE_CIRCLE = 1;

	public float x, y;		// x, y coordinates
	public float dx, dy;		// change in x, y with respect to time
	public float xa, ya;		// change in dx, dy with respect to time


	public float r;			// Radius (If this is type circle, which is all there is right now)

	public boolean movable;

	public float mass;				/* UNUSED UNTIL PHYSICS IS FIXED */

	private int savedX, savedY;

	public long last;

	public float elasticity = 1f;

	public boolean hasMoved() {
		boolean result = ((savedX != (int)x) || (savedY != (int)y));
		savedX = (int)x;
		savedY = (int)y;
		return result;
	}

	public RigidBody() {
		//last = System.currentTimeMillis();
		movable = true;
	}
}
