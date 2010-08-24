package com.glgames.game.ui;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import com.glgames.game.Renderer;

public class UIComponent {
	protected ArrayList<UIComponent> children = new ArrayList<UIComponent>();
	protected UIComponent parent;
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
	UIComponent(Rectangle rect) { 
		this.x = rect.x;
		this.y = rect.y;
		this.width = rect.width;
		this.height = rect.height;
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
	}

	public void setVisibleRecursive(boolean visible) {
		this.visible = visible;
		for (UIComponent child : children) {
			child.setVisibleRecursive(visible);
		}
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setAbsoluteLocation(int abs_x, int abs_y) {
		this.abs_x = abs_x;
		this.abs_y = abs_y;
		for (UIComponent child : children) {
			child.setAbsoluteLocation(this.abs_x + child.x, this.abs_y + child.y);
		}
	}

	public Point getLocation() {
		Point location = new Point(x, y);
		return location;
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
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

	public void addChild(UIComponent child) {
		child.parent = this;
		child.setAbsoluteLocation(this.abs_x + child.x, this.abs_y + child.y);
		this.children.add(child);
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
	
	public void draw(Renderer r) { 
		if (!getVisible()) {
			return;
		}

		drawComponent(r);
		for (UIComponent child : children) {
			child.draw(r);
		}
	}
	
	protected void drawComponent(Renderer r) {
		
	}
}
