package com.glgames.server.physics2d;

public class RigidBody {

	public final static int RBTYPE_CIRCLE = 1;

	public float x, y;		// x, y coordinates
	public float dx, dy;		// change in x, y with respect to time
	public float xa, ya;		// change in dx, dy with respect to time


	public float r;			// Radius (If this is type circle, which is all there is right now)

	public boolean movable;

	float mass;				/* UNUSED UNTIL PHYSICS IS FIXED */

	private float savedX, savedY;

	public boolean hasMoved() {
		boolean result = ((savedX != x) || (savedY != y));
		savedX = x;
		savedY = y;
		return result;
	}

	public RigidBody() {
		movable = true;
	}
}