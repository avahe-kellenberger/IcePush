package com.glgames.game;

import java.awt.event.KeyEvent;

import com.glgames.server.Player;


public class KeyHandler extends BugfixKeyListener {
	static boolean isMoving, isRotating;
	
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		if(!GameObjects.loaded)
			return;
		if(IcePush.DEBUG)
			System.out.println("key pressed");
		int moveDir = -1;
		int rotDir = -1;

		if(IcePush.state == IcePush.WELCOME) {
			int code = e.getKeyCode();
			if(code == KeyEvent.VK_ENTER || code == KeyEvent.VK_TAB) {
				if (GameObjects.serverMode == GameObjects.TYPE_IN_BOX) {
					GameObjects.serverBox.toggleFocused();
					GameObjects.usernameBox.toggleFocused();
				}
			} else if(code == KeyEvent.VK_ESCAPE) {
				IcePush.running = false;
			} else {
				if(GameObjects.serverBox.isFocused())
					GameObjects.serverBox.append(e.getKeyChar());
				else
					GameObjects.usernameBox.append(e.getKeyChar());
			}
		} else switch (e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				IcePush.running = false;
				break;
			case KeyEvent.VK_Q:
				NetworkHandler.logOut();
				break;
			case KeyEvent.VK_UP:
				if (IcePush.renderer instanceof Renderer3D)
					moveDir = Player.FORWARD;
				else
					moveDir = Player.UP;
				break;
			case KeyEvent.VK_DOWN:
				if (IcePush.renderer instanceof Renderer3D)
					moveDir = Player.BACKWARD;
				else
					moveDir = Player.DOWN;
				break;
			case KeyEvent.VK_LEFT:
				if(IcePush.renderer instanceof Renderer3D)
					rotDir = Player.LEFT;
				else
					moveDir = Player.LEFT;
				break;
			case KeyEvent.VK_RIGHT:
				if (IcePush.renderer instanceof Renderer3D)
					rotDir = Player.RIGHT;
				else
					moveDir = Player.RIGHT;
				break;
			case KeyEvent.VK_P:
				NetworkHandler.ping();
				break;
			case KeyEvent.VK_W:
				if(GameObjects.GRAPHICS_MODE == GameObjects.SOFTWARE_3D)
					((Renderer3D) IcePush.renderer).pitch -= 5;
				break;
			case KeyEvent.VK_S:
				if(GameObjects.GRAPHICS_MODE == GameObjects.SOFTWARE_3D)
					((Renderer3D) IcePush.renderer).pitch += 5;
				break;
			case KeyEvent.VK_2:
				IcePush.renderer.switchMode(GameObjects.SOFTWARE_2D);
				break;
			case KeyEvent.VK_3:
				IcePush.renderer.switchMode(GameObjects.SOFTWARE_3D);
				break;
		}
		
		if(moveDir != -1) {
			if(isMoving)
				return;
			
			NetworkHandler.sendMoveRequest(moveDir);
			isMoving = true;
		}
		if(rotDir != -1) {
			if(isRotating)
				return;
			
			NetworkHandler.sendRotationRequest(rotDir);
			isRotating = true;
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
				NetworkHandler.endMoveRequest();
				isMoving = false;
				break;
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_RIGHT:
				if (IcePush.renderer instanceof Renderer3D) {
					NetworkHandler.endRotationRequest();
					isRotating = false;
				} else {
					NetworkHandler.endMoveRequest();
					isMoving = false;
				}
				break;
		}
	}
}
