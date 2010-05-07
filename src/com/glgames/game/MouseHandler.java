package com.glgames.game;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseHandler extends MouseAdapter {

	public void mouseClicked(MouseEvent e) {
		if(GameEngine.state != GameEngine.WELCOME)
			return;
		if (GameObjects.loginButton.contains(e.getPoint())) {
			NetworkHandler.login(GameObjects.serverBox.getText(),
					GameObjects.usernameBox.getText());
		}
	}
}
