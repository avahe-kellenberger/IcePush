package com.glgames.game;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.glgames.game.ui.ServerList;
import com.glgames.game.ui.TextBox;
import com.glgames.game.ui.UserInterface;

public class GameObjects {
	static final int SERVER_IFACE = 1;
	static final int USERNAME_IFACE = 2;
	
	public static boolean loaded = false;
	
	public static String[] instructions, help;
	public static BufferedImage logo;
	public static BufferedImage background;

	public static BufferedImage button, button2;

	public static TextBox serverBox, usernameBox;
	public static ServerList serverList;
	public static Rectangle loginButton, helpButton, backButton;

	public static Player[] players;
	public static Object3D[] scenery; // Right now scenery is only used in 3D mode
	
	// World list stuff
	public static final int TYPE_IN_BOX = 0;
	public static final int LIST_FROM_SERVER = 1;
	public static final int USE_DEFAULT = 3;
	public static int serverMode = TYPE_IN_BOX;

	// Loading bar stuff
	public static String loadingMessage = "Loading title screen...";
	public static int loadingPercent = 10;

	public static void load() {

		try {
			UserInterface.load();
			serverBox = (TextBox) UserInterface.interfaces[SERVER_IFACE];
			usernameBox = (TextBox) UserInterface.interfaces[USERNAME_IFACE];
			
		//	trySleep(500);
			loadingMessage = "Loading text...";
			loadingPercent = 30;
			
			instructions = new String[] { "Push the other players off the ice!",
					"Try not to fall off!" };
			help = new String[] { "Arrow keys - move", "Q - logout", "2 - 2D view", "3 - 3D view"};
			logo = SpriteLoader.getSprite("images/logo.png");
			button = SpriteLoader.getSprite("images/button.png");
			button2 = SpriteLoader.getSprite("images/button2.png");			
			
			loginButton = new Rectangle(IcePush.WIDTH / 2 - 110, 330, 100, 25);
			helpButton = new Rectangle(IcePush.WIDTH / 2 + 0, 330, 100, 25);
			backButton = new Rectangle(IcePush.WIDTH / 2 - 50, 330, 100, 25);
			
		//	trySleep(300);
			loadingMessage = "Loading players...";
			loadingPercent = 50;

			if (!IcePush.isApplet)
				serverList = new ServerList(170);
			
			scenery = new Object3D[10]; 
			scenery[0] = new Object3D.Plane(0, 0, 0, 16, 10, 50);

		//	trySleep(400);
			loadingMessage = "Loading images...";
			loadingPercent = 80;
			
			background = SpriteLoader.getSprite("images/icepush.png");
			
		//	trySleep(600);
			loadingMessage = "Done";
			loadingPercent = 99; // rounding error and such
			
		//	trySleep(200);
			loaded = true;
		} catch(Exception e) {
			loadingMessage = loadingMessage + " - " + e.toString();
			loadingPercent = -1;
			e.printStackTrace();
		}
	}
	
	public static void trySleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch(Exception e) { }
	}
}
