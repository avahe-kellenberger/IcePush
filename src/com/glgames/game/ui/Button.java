package com.glgames.game.ui;

import java.awt.Color;
import java.awt.Graphics;

import com.glgames.game.IcePush;

public class Button extends UIComponent {
	String caption;
	Color bgcol, fgcol;
	
	protected void drawComponent(Graphics g) {
		g.setColor(bgcol);
		g.fill3DRect(x, y, width, height, true);
		g.setColor(fgcol);
		int w = g.getFontMetrics().stringWidth(caption);
		g.drawString(caption, x + width / 2 - w / 2, y + 18);
	}
	

	public static Action<Button> helpAction = new Action<Button>() {
		public void action(Button b, int x, int y) {
			System.out.println("help");
			IcePush.state = IcePush.HELP;
		}
	}, backAction = new Action<Button>() {
		public void action(Button b, int x, int y) {
			IcePush.state = IcePush.WELCOME;
		}
	};
}
