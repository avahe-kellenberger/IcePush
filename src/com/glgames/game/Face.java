package com.glgames.game;

import java.awt.Color;

public class Face implements Comparable<Face> {
	public Face(int[] dx, int dy[], int np, double d, Color c) {
		drawX = dx;
		drawY = dy;
		numPoints = np;
		distance = d;
		color = c;
	}

	public void draw() {
		Triangles.solidTriangle(drawX[0], drawY[0], drawX[1], drawY[1],
				drawX[2], drawY[2], color.getRGB());
	}

	public int compareTo(Face obj) {
		return distance > obj.distance ? 1 : -1;
	}

	public final int drawX[];
	public final int drawY[];
	public final int numPoints;
	public final double distance;
	public final Color color;
}
