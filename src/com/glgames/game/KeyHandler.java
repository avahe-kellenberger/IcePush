package com.glgames.game;

import java.awt.event.KeyEvent;

import com.glgames.server.Player;


public class KeyHandler extends BugfixKeyListener {
	static boolean isMoving;
	
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		if(!GameObjects.loaded)
			return;
		if(IcePush.DEBUG)
			System.out.println("key pressed");
		int moveDir = -1;

		if(GameEngine.state == GameEngine.WELCOME) {
			int code = e.getKeyCode();
			if(code == KeyEvent.VK_ENTER || code == KeyEvent.VK_TAB) {
				if (GameObjects.serverMode == GameObjects.TYPE_IN_BOX) {
					GameObjects.serverBox.toggleFocused();
					GameObjects.usernameBox.toggleFocused();
				}
			} else if(code == KeyEvent.VK_ESCAPE) {
				GameEngine.running = false;
			} else {
				if(GameObjects.serverBox.isFocused())
					GameObjects.serverBox.append(e.getKeyChar());
				else
					GameObjects.usernameBox.append(e.getKeyChar());
			}
		} else switch (e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				GameEngine.running = false;
				break;
			case KeyEvent.VK_Q:
				NetworkHandler.logOut();
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
			case KeyEvent.VK_P:
				NetworkHandler.ping();
				break;
			case KeyEvent.VK_W:
				if(GameObjects.GRAPHICS_MODE == GameObjects.THREE_D)
					((Renderer3D) GameEngine.frame.getRenderer()).pitch -= 5;
				break;
			case KeyEvent.VK_A:
				if(GameObjects.GRAPHICS_MODE == GameObjects.THREE_D)
					((Renderer3D) GameEngine.frame.getRenderer()).yaw -= 5;
				break;
			case KeyEvent.VK_S:
				if(GameObjects.GRAPHICS_MODE == GameObjects.THREE_D)
					((Renderer3D) GameEngine.frame.getRenderer()).pitch += 5;
				break;
			case KeyEvent.VK_D:
				if(GameObjects.GRAPHICS_MODE == GameObjects.THREE_D)
					((Renderer3D) GameEngine.frame.getRenderer()).yaw += 5;
				break;
		}
		
		if(moveDir != -1) {
			if(isMoving)
				return;
			
			NetworkHandler.sendMoveRequest(moveDir);
			isMoving = true;
		}
	}

	public void keyReleased(KeyEvent e) {
		super.keyReleased(e);
		if (!getReleased())
			return;
		if (IcePush.DEBUG)
			System.out.println("key released");
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
}
