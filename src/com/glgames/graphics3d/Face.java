package com.glgames.graphics3d;

import java.awt.Color;

public class Face implements Comparable<Face> {
	public Face(int[] dx, int dy[], int[] z, int np, double d, Color c) {
		drawX = dx;
		drawY = dy;
		numPoints = np;
		distance = d;
		color = c;
		drawZ = z;
	}
	
	public int compareTo(Face obj) {
		return distance > obj.distance ? 1 : -1;
	}
	
	public final int drawX[];
	public final int drawY[];
	public final int drawZ[];
	public final int numPoints;
	public final double distance;
	public final Color color;
}
