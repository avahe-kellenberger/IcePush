package com.glgames.game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.glgames.server.Player;


public class KeyHandler implements KeyListener {
	private boolean isMoving;
	
	public void keyPressed(KeyEvent e) {
		if(!GameObjects.loaded)
			return;
		
		int moveDir = -1;
		switch (e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				GameEngine.running = false;
				break;
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_TAB:
				if(GameEngine.state != GameEngine.WELCOME)
					break;
				
				GameObjects.serverBox.toggleFocused();
				GameObjects.usernameBox.toggleFocused();
				break;
			case KeyEvent.VK_UP:
				moveDir = Player.UP;
				break;
			case KeyEvent.VK_DOWN:
				moveDir = Player.DOWN;
				break;
			case KeyEvent.VK_LEFT:
				moveDir = Player.LEFT;
				break;
			case KeyEvent.VK_RIGHT:
				moveDir = Player.RIGHT;
				break;
			default:
				if(GameEngine.state == GameEngine.WELCOME) {
					if(GameObjects.serverBox.isFocused())
						GameObjects.serverBox.append(e.getKeyChar());
					else
						GameObjects.usernameBox.append(e.getKeyChar());
				}
				
				break;
		}
		
		if(moveDir != -1) {
			if(isMoving)
				return;
			
			// checks collision too
			NetworkHandler.sendMoveRequest(moveDir);
			isMoving = true;
		}
	}

	public void keyReleased(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_RIGHT:
				NetworkHandler.endMoveRequest();
				isMoving = false;
				break;
		}
	}

	public void keyTyped(KeyEvent e) {

	}

}
