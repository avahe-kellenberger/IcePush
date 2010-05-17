package com.glgames.game;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseHandler extends MouseAdapter {

	public void mouseClicked(MouseEvent e) {
		if(GameEngine.state != GameEngine.WELCOME || !GameObjects.loaded)
			return;
		
		if(GameObjects.serverMode == GameObjects.LIST_FROM_SERVER)
			GameObjects.serverList.processClick(e.getX(), e.getY());
		
		if (GameObjects.loginButton.contains(e.getPoint())) {
			String server;
			if (GameObjects.serverMode == GameObjects.LIST_FROM_SERVER)
				server = GameObjects.serverList.getSelected();
			else
				server = GameObjects.serverBox.getText();
			NetworkHandler.login(server, GameObjects.usernameBox.getText());
		}
	}
}
