package com.glgames.ui;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

public class UIComponent {
	protected ArrayList<UIComponent> children = new ArrayList<UIComponent>();
	protected Container parent = null;
	protected Action clickAction = null;
	protected Action hoverAction = null;
	protected Action unhoverAction = null;
	protected Action mousePressAction = null;
	protected Action mouseReleaseAction = null;

	protected int x, y;
	protected int abs_x, abs_y;
	protected int width, height;
	protected boolean visible = false;

	UIComponent(int x, int y, int width, int height) { 
		this.x = x;
		this.y = y;
		this.abs_x = x;
		this.abs_y = y;
		this.width = width;
		this.height = height;
	}
	UIComponent(int width, int height) { 
		// Use this constructor when making a component that will be auto-positioned by a container
		this.x = 0;
		this.y = 0;
		this.abs_x = x;
		this.abs_y = y;
		this.width = width;
		this.height = height;
	}
	
	public void setClickAction(Action clickAction) {
		this.clickAction = clickAction;
	}

	public Action getClickAction() {
		return clickAction;
	}

	public void setMousePressAction(Action mousePressAction) {
		this.mousePressAction = mousePressAction;
	}

	public Action getMousePressAction() {
		return mousePressAction;
	}

	public void setMouseReleaseAction(Action mouseReleaseAction) {
		this.mouseReleaseAction = mouseReleaseAction;
	}

	public Action getMouseReleaseAction() {
		return mouseReleaseAction;
	}

	public void setHoverAction(Action hoverAction) {
		this.hoverAction = hoverAction;
	}

	public Action getHoverAction() {
		return hoverAction;
	}

	public void setUnhoverAction(Action unhoverAction) {
		this.unhoverAction = unhoverAction;
	}

	public Action getUnhoverAction() {
		return unhoverAction;
	}

	public boolean getVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		if (parent != null)
			parent.positionChildren();
	}

	public void setVisibleRecursive(boolean visible) {
		this.visible = visible;
		for (UIComponent child : children) {
			child.setVisibleRecursive(visible);
		}
		if (parent != null)
			parent.positionChildren();
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
		updateAbsoluteLocation();
	}

	public Point getLocation() {
		Point location = new Point(x, y);
		return location;
	}

	public void setAbsoluteLocation(int abs_x, int abs_y) {
		this.abs_x = abs_x;
		this.abs_y = abs_y;
		for (UIComponent child : children) {
			child.setAbsoluteLocation(this.abs_x + child.x, this.abs_y + child.y);
		}
	}

	public void updateAbsoluteLocation() {
		// Recalculate absolute location based on the parent
		if (parent != null)
			setAbsoluteLocation(parent.abs_x + x, parent.abs_y + y);
		else
			setAbsoluteLocation(x, y);
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		if (parent != null)
			parent.positionChildren();
	}

	public Dimension getSize() {
		Dimension size = new Dimension(width, height);
		return size;
	}

	public Rectangle getRect() {
		Rectangle rect = new Rectangle(abs_x, abs_y, width, height);
		return rect;
	}

	public UIComponent getParent() {
		return parent;
	}

	public boolean handleAction(Actions actionType, int x, int y) {
		// Run the received action if the component accepts it
		boolean result;
		Action action = null;
		if (getVisible() && getRect().contains(x, y)) {
			if (actionType == Actions.CLICK)
				action = getClickAction();
			else if (actionType == Actions.HOVER)
				action = getHoverAction();
			else if (actionType == Actions.PRESS)
				action = getMousePressAction();
			else if (actionType == Actions.RELEASE)
				action = getMouseReleaseAction();
			if (action != null) {
				action.doAction(this, x, y);
				// Consume the action
				return true;
			}

			for (UIComponent child : children) {
				result = child.handleAction(actionType, x, y);
				// If an action was consumed, pass it up
				if (result)
					return true;
			}
		} else if (actionType == Actions.HOVER) {
			// If we were passed a HOVER action, but it is not in the component rectangle,
			// then treat it as an UNHOVER action
			action = getUnhoverAction();
			if (action != null) {
				action.doAction(this, x, y);
			}
			for (UIComponent child : children) {
				result = child.handleAction(actionType, x, y);
				// If an action was consumed, pass it up
				if (result)
					return true;
			}
		}
		return false;
	}
	
	public void draw(Graphics g) { 
		if (!getVisible()) {
			return;
		}

		drawComponent(g);
		for (UIComponent child : children) {
			child.draw(g);
		}
	}
	
	protected void drawComponent(Graphics g) {
		
	}
}
