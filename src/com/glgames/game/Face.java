package com.glgames.game;

import java.awt.Color;
import java.awt.Graphics;

public class Face implements Comparable<Face> {
	public Face(int[] dx, int dy[], int np, double d, Color c) {
		drawX = dx;
		drawY = dy;
		numPoints = np;
		distance = d;
		color = c;
	}

	public void draw(Graphics g) {
		g.setColor(color);
		g.fillPolygon(drawX, drawY, numPoints);
		// Triangles.solidTriangle(drawX[0], drawY[0], drawX[1], drawY[1],
		// 		drawX[2], drawY[2], color.getRGB());
	}

	public int compareTo(Face obj) {
		return distance > obj.distance ? 1 : -1;
	}

	private final int drawX[];
	private final int drawY[];
	private final int numPoints;
	private final double distance;
	private final Color color;
	// private int centerDrawX, centerDrawY;
}
