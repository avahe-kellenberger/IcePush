package com.glgames.game;

import java.awt.Color;

public class Face {
	public Face(int[] dx, int dy[], int[] z, int np, double d, Color c, Texture t) {
		drawX = dx;
		drawY = dy;
		numPoints = np;
		distance = d;
		color = c;
		texture = t;
		drawZ = z;
	}
	
	public final int drawX[];
	public final int drawY[];
	public final int drawZ[];
	public final int numPoints;
	public final double distance;
	public final Color color;
	public final Texture texture;
}

class Triangle implements Comparable<Triangle> {
	public int x1, y1, x2, y2, x3, y3;
	public int z1, z2, z3;
	public double distance;
	public Color color;
	public Texture texture;
	
	public int compareTo(Triangle obj) {
		return distance > obj.distance ? 1 : -1;
	}
}
