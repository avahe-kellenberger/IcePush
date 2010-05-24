package com.glgames.game;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseHandler extends MouseAdapter {

	public void mouseClicked(MouseEvent e) {
		if (!GameObjects.loaded)
			return;

		if(IcePush.state == IcePush.WELCOME) {
			if(GameObjects.serverMode == GameObjects.LIST_FROM_SERVER)
				GameObjects.serverList.processClick(e.getX(), e.getY());
			
			if (GameObjects.loginButton.contains(e.getPoint())) {
				String server;
				if (GameObjects.serverMode == GameObjects.LIST_FROM_SERVER)
					server = GameObjects.serverList.getSelected();
				else
					server = GameObjects.serverBox.getText();
				if (!server.isEmpty())
					NetworkHandler.login(server, GameObjects.usernameBox.getText());
			} else if (GameObjects.helpButton.contains(e.getPoint())) {
				IcePush.state = IcePush.HELP;
			}
		} else if(IcePush.state == IcePush.HELP) {
			if (GameObjects.backButton.contains(e.getPoint())) {
				IcePush.state = IcePush.WELCOME;
			}
		}
	}
}
