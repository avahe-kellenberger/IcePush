package com.glgames.ui;

import java.awt.Color;
import java.awt.Rectangle;

import com.glgames.graphics2d.Renderer;

public class Button extends UIComponent {
	private Color HOVER_BGCOLOR = Color.lightGray;
	private Color NORMAL_BGCOLOR = Color.gray;

	protected Color bgColor = NORMAL_BGCOLOR;
	protected Color fgColor = Color.white;
	protected String caption;
	protected Boolean depressed = false;
	
	Button (int x, int y, int width, int height) {
		super(x, y, width, height);
		hoverAction = new Action<Button>() {
			public void doAction(Button component, int x, int y) {
				component.setBG(HOVER_BGCOLOR);
			}
		};
		unhoverAction = new Action<Button>() {
			public void doAction(Button component, int x, int y) {
				component.setBG(NORMAL_BGCOLOR);
				component.setDepressed(false);
			}
		};
		mousePressAction = new Action<Button>() {
			public void doAction(Button component, int x, int y) {
				component.setBG(NORMAL_BGCOLOR);
				component.setDepressed(true);
			}
		};
		mouseReleaseAction = new Action<Button>() {
			public void doAction(Button component, int x, int y) {
				component.setDepressed(false);
			}
		};
	}
	Button (int width, int height) {
		super(width, height);
		hoverAction = new Action<Button>() {
			public void doAction(Button component, int x, int y) {
				component.setBG(HOVER_BGCOLOR);
			}
		};
		unhoverAction = new Action<Button>() {
			public void doAction(Button component, int x, int y) {
				component.setBG(NORMAL_BGCOLOR);
				component.setDepressed(false);
			}
		};
		mousePressAction = new Action<Button>() {
			public void doAction(Button component, int x, int y) {
				component.setBG(NORMAL_BGCOLOR);
				component.setDepressed(true);
			}
		};
		mouseReleaseAction = new Action<Button>() {
			public void doAction(Button component, int x, int y) {
				component.setDepressed(false);
			}
		};
	}

	public void setFG(Color fgColor) {
		this.fgColor = fgColor;
	}

	public void setBG(Color bgColor) {
		this.bgColor = bgColor;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}
	
	public String getCaption() {
		return caption;
	}

	public void setDepressed(Boolean depressed) {
		this.depressed = depressed;
	}

	public Boolean getDepressed() {
		return depressed;
	}

	protected void drawComponent(Renderer r) {
		r.setColor(bgColor);
		r.fill3DRect(abs_x, abs_y, width, height, !depressed);
		r.setColor(fgColor);
		int w = r.stringWidth(caption);
		r.drawString(caption, abs_x + width / 2 - w / 2, abs_y + 18);
	}
}