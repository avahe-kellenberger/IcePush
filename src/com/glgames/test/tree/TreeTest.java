package com.glgames.test.tree;

import static java.awt.AWTEvent.MOUSE_EVENT_MASK;
import static java.awt.AWTEvent.WINDOW_EVENT_MASK;
import static java.awt.event.WindowEvent.WINDOW_CLOSING;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

public class TreeTest extends Frame {

	private static int width = 800;
	private static int height = 600;

	private int insLeft, insTop;

	QuadTree tree;

	public static void main(String thusly[]) {
		new TreeTest();
	}

	TreeTest() {
		super("QUADRATIC TREE TEST");
		tree = new QuadTree(width, height);
		enableEvents(WINDOW_EVENT_MASK | MOUSE_EVENT_MASK);
		setResizable(false);
		setVisible(true);
		Insets i = getInsets();
		insTop = i.top;
		insLeft = i.left;
		setSize(width + insLeft + i.right + 1, height + insTop + i.bottom + 1);
	}

	public void processMouseEvent(MouseEvent me) {
		if(me.getID() != MouseEvent.MOUSE_CLICKED) return;
		QtNode n = tree.root.getNodeContaining(me.getX() - insLeft, me.getY() - insTop);
		if(n != null) n.split();
		repaint();
	}

	public void processWindowEvent(WindowEvent we) {
		if(we.getID() == WINDOW_CLOSING) dispose();
	}

	public void paint(Graphics pg) {
		pg.translate(insLeft, insTop);
		pg.setColor(Color.BLACK);
		renderQuadTree(pg);
	}


	public void update(Graphics g) {
		paint(g);
	}

	private void renderQuadTree(Graphics g) {
		renderNode(tree.root, g);
	}

	private void renderNode(QtNode qt, Graphics g) {
		if(qt != null) {
			g.drawRect(qt.x, qt.y, qt.w, qt.h);
			renderNode(qt.n1, g);
			renderNode(qt.n2, g);
			renderNode(qt.n3, g);
			renderNode(qt.n4, g);
		}
	}
}
