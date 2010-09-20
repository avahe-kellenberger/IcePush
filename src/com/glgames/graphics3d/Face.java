package com.glgames.graphics3d;


class Face implements Comparable<Face> {
	int[] x, y, u, v;
	float[] z;
	int texID, color;
	double distance;
	
	public Face(int verts) {
		x = new int[verts];
		y = new int[verts];
		u = new int[verts];
		v = new int[verts];
		z = new float[verts];
	}
	
	public int compareTo(Face o) {
		return distance < o.distance ? -1 : 1;
	}
}
