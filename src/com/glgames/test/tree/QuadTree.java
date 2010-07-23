package com.glgames.test.tree;

public class QuadTree {

	public QuadTree(int w, int h) {
		root = new QtNode(null, 0, 0, w, h);
	}

	QtNode root;
}

class QtNode {

	private QtNode parent;

	int x, y;
	int w, h;

	private int cx, cy;	/* Center x and y */
	private int w2, h2;

	QtNode n1;	/*	Top left	*/
	QtNode n2;	/*	Top right	*/
	QtNode n3;	/*	Bottom left	   */
	QtNode n4;	/*	Bottom right   */

	QtNode(QtNode p, int x, int y, int w, int h) {
		parent = p;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		w2 = w/2;
		h2 = h/2;
		cx = x + w2;
		cy = y + h2;
	}

	public void split() {
		n1 = new QtNode(this, x, y, w2, h2);
		n2 = new QtNode(this, cx, y, w2, h2);
		n3 = new QtNode(this, x, cy, w2, h2);
		n4 = new QtNode(this, cx, cy, w2, h2);
	}

	public QtNode getParent() {
		return parent;
	}

	public void collapse() {
		n1 = n2 = n3 = n4 = null;
	}

	public QtNode getNodeContaining(int px, int py) {
		if(px < x || px - w >= x || py < y || py - h >= y) return null;

		int q = (px >= cx) ? 1 : 0;
		if(py >= cy) q |= 2;

		switch(q) {
			case 0:	return n1 == null ? this : n1.getNodeContaining(px, py);
			case 1:	return n2 == null ? this : n2.getNodeContaining(px, py);
			case 2:	return n3 == null ? this : n3.getNodeContaining(px, py);
			case 3:	return n4 == null ? this : n4.getNodeContaining(px, py);
		}
		throw new InternalError("1 | 2 != 3 Check for severe hardware error");
	}
}