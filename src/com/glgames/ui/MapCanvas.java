package com.glgames.ui;

import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.GeneralPath;


public class MapCanvas extends Container {
	public enum Tool {
		SELECT, LINE, QUADRATIC, CUBIC;
	}

	protected GeneralPath path = new GeneralPath();
	protected Tool tool = Tool.SELECT;
	protected ArrayList<Point> queue = new ArrayList<Point>();
	protected Color bgColor = new Color(255, 255, 255, 60);
	protected Color fgColor = Color.black;

	MapCanvas (int x, int y, int width, int height) {
		super(x, y, width, height);
		clickAction = new Action<MapCanvas>() {
			public void doAction(MapCanvas component, int x, int y) {
				component.queue.add(new Point(x, y));
				component.checkQueue();
			}
		};
	}

	public void checkQueue() {
		/* Compare the points in the queue and the tool selected.
		 * If there are enough points to complete the operation,
		 * then perform it and clear the queue.
		 */
		Point currentPoint;
		Point pointOne;
		Point pointTwo;
		Point pointThree;
		int queueLength = queue.size();

		if ((queueLength == 1) && (tool == Tool.SELECT)) {
			// Put code here to select existing points
		} else if ((tool != Tool.SELECT) && (path.getCurrentPoint() == null)) {
			currentPoint = queue.get(0);
			path.moveTo(currentPoint.x, currentPoint.y);
			queue.remove(0);
		}

		queueLength = queue.size();
		if ((tool == Tool.LINE) && (queueLength == 1)) {
			currentPoint = queue.get(0);
			path.lineTo(currentPoint.x, currentPoint.y);
			queue.remove(0);
		} else if ((tool == Tool.QUADRATIC) && (queueLength == 2)) {
			pointOne = queue.get(0);
			pointTwo = queue.get(1);
			path.quadTo(pointOne.x, pointOne.y, pointTwo.x, pointTwo.y);
			queue.remove(1);
			queue.remove(0);
		} else if ((tool == Tool.CUBIC) && (queueLength == 3)) {
			pointOne = queue.get(0);
			pointTwo = queue.get(1);
			pointThree = queue.get(2);
			path.curveTo(pointOne.x, pointOne.y, pointTwo.x, pointTwo.y, pointThree.x, pointThree.y);
			queue.remove(2);
			queue.remove(1);
			queue.remove(0);
		}
	}

	public void setTool(Tool tool) {
		this.tool = tool;
	}

	public Tool getTool() {
		return tool;
	}

	public void addPoint(int x, int y) {
		queue.add(new Point(x, y));
	}

	protected void drawComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(bgColor);
		g2.fillRect(abs_x, abs_y, width, height);
		g2.setColor(fgColor);
		g2.draw(path);
	}
}
