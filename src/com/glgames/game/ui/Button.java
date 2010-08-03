package com.glgames.game.ui;

import java.awt.Color;

import com.glgames.game.IcePush;
import com.glgames.game.Renderer;

public class Button extends UIComponent {
	String caption;
	Color bgcol, fgcol;
	
	protected void drawComponent(Renderer r) {
		r.setColor(bgcol);
		r.fill3DRect(x, y, width, height, true);
		r.setColor(fgcol);
		int w = r.stringWidth(caption);
		r.drawString(caption, x + width / 2 - w / 2, y + 18);
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
