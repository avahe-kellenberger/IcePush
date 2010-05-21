package com.glgames.game;

class Vector2d {
	public double x, y;

	public double length() {
		return Math.sqrt(x * x + y * y);
	}

	public void normalize() {
		double l = length();
		x /= l;
		y /= l;
	}

	public void add(Vector2d b) {
		x += b.x;
		y += b.y;
	}

	public void sub(Vector2d b) {
		x -= b.x;
		y -= b.y;
	}

	public double dot(Vector2d b) {
		return x * b.x + y * b.y;
	}

	public void mult(Vector2d b) {
		x *= b.x;
		y *= b.y;
	}
	
	public Vector2d copy() {
		Vector2d v = new Vector2d();
		v.x = x;
		v.y = y;
		return v;
	}
}

class Vector3d {
	public double x, y, z;

	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public void normalize() {
		double l = length();
		x /= l;
		y /= l;
		z /= l;
	}

	public void add(Vector3d b) {
		x += b.x;
		y += b.y;
		z += b.z;
	}

	public void sub(Vector3d b) {
		x -= b.x;
		y -= b.y;
		z -= b.z;
	}

	public double dot(Vector3d b) {
		return x * b.x + y * b.y + z * b.z;
	}

	public void mult(Vector3d b) {
		x *= b.x;
		y *= b.y;
		z *= b.z;
	}
	
	public Vector3d copy() {
		Vector3d v = new Vector3d();
		v.x = x;
		v.y = y;
		v.z = z;
		return v;
	}
}