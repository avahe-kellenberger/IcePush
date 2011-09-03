package com.glgames.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class Button extends UIComponent {
	private Color HOVER_BGCOLOR = Color.lightGray;
	private Color NORMAL_BGCOLOR = Color.gray;

	protected Color bgColor = NORMAL_BGCOLOR;
	protected Color fgColor = Color.white;
	protected String caption;
	protected Boolean depressed = false;
	protected Font font = new Font("Arial", Font.PLAIN, 18);
	
	Button (int x, int y, int width, int height) {
		super(x, y, width, height);
		hoverAction = new Action() {
			public void doAction(UIComponent uiComp, int x, int y) {
				Button comp = (Button) uiComp;
				comp.setBG(HOVER_BGCOLOR);
			}
		};
		unhoverAction = new Action() {
			public void doAction(UIComponent uiComp, int x, int y) {
				Button comp = (Button) uiComp;
				comp.setBG(NORMAL_BGCOLOR);
				comp.setDepressed(false);
			}
		};
		mousePressAction = new Action() {
			public void doAction(UIComponent uiComp, int x, int y) {
				Button comp = (Button) uiComp;
				comp.setBG(NORMAL_BGCOLOR);
				comp.setDepressed(true);
			}
		};
		mouseReleaseAction = new Action() {
			public void doAction(UIComponent uiComp, int x, int y) {
				Button comp = (Button) uiComp;
				comp.setDepressed(false);
			}
		};
	}
	Button (int width, int height) {
		super(width, height);
		hoverAction = new Action() {
			public void doAction(UIComponent uiComp, int x, int y) {
				Button comp = (Button) uiComp;
				comp.setBG(HOVER_BGCOLOR);
			}
		};
		unhoverAction = new Action() {
			public void doAction(UIComponent uiComp, int x, int y) {
				Button comp = (Button) uiComp;
				comp.setBG(NORMAL_BGCOLOR);
				comp.setDepressed(false);
			}
		};
		mousePressAction = new Action() {
			public void doAction(UIComponent uiComp, int x, int y) {
				Button comp = (Button) uiComp;
				comp.setBG(NORMAL_BGCOLOR);
				comp.setDepressed(true);
			}
		};
		mouseReleaseAction = new Action() {
			public void doAction(UIComponent uiComp, int x, int y) {
				Button comp = (Button) uiComp;
				comp.setDepressed(false);
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

	public void setFont(Font font) {
		this.font = font;
	}

	public Font getFont() {
		return font;
	}

	protected void drawComponent(Graphics g) {
		g.setColor(bgColor);
		g.fill3DRect(abs_x, abs_y, width, height, !depressed);
		g.setColor(fgColor);
		g.setFont(font);
		int captionWidth = g.getFontMetrics().stringWidth(caption);
		int fontHeight = g.getFontMetrics().getHeight();
		int fontDescent = g.getFontMetrics().getDescent();
		// Subtract 1 at the end of the height calculation because it looks more centered on the 3D button that way
		g.drawString(caption, (abs_x + width / 2 - captionWidth / 2), (abs_y + height / 2 + fontHeight / 2 - fontDescent - 1));
	}
}
