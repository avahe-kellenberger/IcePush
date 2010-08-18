package com.glgames.game;

import java.awt.image.BufferedImage;

import com.glgames.game.ui.*;

public class GameObjects {
	static final int SERVER_IFACE = 1;
	static final int USERNAME_IFACE = 2;
	static final int SERVER_LIST_IFACE = 6;
	
    public static UI ui;

	public static boolean loaded = false;
	
	public static String[] instructions, help;
	public static BufferedImage logo;
	public static BufferedImage background;

	public static BufferedImage button, button2;
	public static TextBox serverBox, usernameBox;
	public static ServerList serverList;

	public static Player[] players;
	public static Object3D[] scenery; // Right now scenery is only used in 3D mode
	public static String error;
	
	// World list stuff
	public static final int TYPE_IN_BOX = 0;
	public static final int LIST_FROM_SERVER = 1;
	public static final int USE_DEFAULT = 3;
	public static int serverMode = TYPE_IN_BOX;
	
	public static void load() {

		try {
            ui = new UI(0, 0, IcePush.WIDTH, IcePush.HEIGHT);
			if(serverMode == LIST_FROM_SERVER) {
				ui.serverTextBox.setVisible(false);
			} else if(serverMode == TYPE_IN_BOX) {
				ui.serverList.setVisible(false);
			} else {
				ui.serverTextBox.setVisible(false);
				ui.serverList.setVisible(false);
			}
			
            ui.serverTextBox.setText(NetworkHandler.DEFAULT_SERVER);
            ui.serverTextBox.setAction(IcePush.onServerTextBoxClick);
            ui.usernameTextBox.setAction(IcePush.onUsernameTextBoxClick);
            ui.serverList.setAction(ServerList.onServerListClick);
            ui.loginButton.setAction(NetworkHandler.onLoginButtonClick);
            ui.helpButton.setAction(IcePush.onHelpButtonClick);
            ui.backButton.setAction(IcePush.onBackButtonClick);

			instructions = new String[] { "Push the other players off the ice!",
					"Try not to fall off!" };
			help = new String[] { "Arrow keys - move", "Q - logout", "2 - 2D view", "3 - 3D view", "C - chat" };
			logo = SpriteLoader.getSprite("images/logo.png");
			button = SpriteLoader.getSprite("images/button.png");
			button2 = SpriteLoader.getSprite("images/button2.png");			
			
			Map.load();
			scenery = new Object3D[10]; 
			scenery[0] = new Object3D.Plane(0, 0, 0, 20, 12, 40);
			
			background = SpriteLoader.getSprite("images/icepush.png");
			loaded = true;
		} catch(Exception e) {
			error = e.toString();
			e.printStackTrace();
		}
	}
}
