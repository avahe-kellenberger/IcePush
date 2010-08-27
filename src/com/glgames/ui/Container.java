package com.glgames.ui;

import java.awt.Color;

import com.glgames.graphics2d.Renderer;

public class Container extends UIComponent {
	public enum Layout {
		FIXED, HORIZONTAL, VERTICAL;
	}

	protected Layout layout = Layout.HORIZONTAL;

	Container (int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public void setLayout(Layout layout) {
		this.layout = layout;
	}

	public Layout getLayout() {
		return layout;
	}

	public void addChild(UIComponent child) {
		child.parent = this;
		this.children.add(child);
	}

	public void positionChildren() {
		int unit; // The unit of spacing between components
		int amount = 1; // The amount of units from the edge to place component
		int childrenVisible = 0;
		int top, left;

		for (UIComponent child : this.children)
			if (child.getVisible())
				childrenVisible++;

		if (layout == Layout.HORIZONTAL) {
			for (UIComponent child : this.children) {
				if (!child.getVisible())
					continue;

				unit = this.width / (childrenVisible * 2);
				top = (this.height / 2) - (child.height / 2);
				left = (unit * amount) - (child.width / 2);
				child.setLocation(left, top);
				amount += 2;
			}
		} else if (layout == Layout.VERTICAL) {
			for (UIComponent child : this.children) {
				if (!child.getVisible())
					continue;

				unit = this.height / (childrenVisible * 2);
				left = (this.width / 2) - (child.width / 2);
				top = (unit * amount) - (child.height / 2);
				child.setLocation(left, top);
				amount += 2;
			}
		}
	}
}
