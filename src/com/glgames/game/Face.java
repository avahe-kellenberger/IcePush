package com.glgames.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.TexturePaint;

public class Face implements Comparable<Face> {
	public Face(int[] dx, int dy[], int np, double d, Color c, TexturePaint txt) {
		drawX = dx;
		drawY = dy;
		numPoints = np;
		distance = d;
		color = c;
		texture = txt;
	}

	public void draw(Graphics g) {
		if(texture != null)
			((Graphics2D) g).setPaint(texture);
		else
			g.setColor(color);
		System.out.println(((Graphics2D) g).getPaint());
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
	private final TexturePaint texture;
	// private int centerDrawX, centerDrawY;
}
