package net.threesided.server.physics2d;

import net.threesided.shared.Vector2D;

public class RigidBody {

	public Vector2D position = new Vector2D();		// x, y coordinates
	public Vector2D velocity = new Vector2D();		// change in x, y with respect to time
	public Vector2D acceleration = new Vector2D();		// change in dx, dy with respect to time


	public double r;			// Radius (If this is type circle, which is all there is right now)

	public boolean movable;

	public double mass;

	private int savedX, savedY;

	public boolean hasMoved() {
		boolean result = ((savedX != (int) position.getX()) || (savedY != (int) position.getY()));
		savedX = (int)position.getX();
		savedY = (int)position.getY();
		return result;
	}

	public RigidBody() {
		movable = true;
	}
}