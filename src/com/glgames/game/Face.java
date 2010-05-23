package com.glgames.game;

import java.awt.Color;

public class Face {
	public Face(int[] dx, int dy[], int np, double d, Color c) {
		drawX = dx;
		drawY = dy;
		numPoints = np;
		distance = d;
		color = c;
	}
	
	public final int drawX[];
	public final int drawY[];
	public final int numPoints;
	public final double distance;
	public final Color color;
}

class Triangle implements Comparable<Triangle> {
	public int x1, y1, x2, y2, x3, y3;
	public double distance;
	public Color color;
	
	public int compareTo(Triangle obj) {
		return distance > obj.distance ? 1 : -1;
	}
}
