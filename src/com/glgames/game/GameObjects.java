package com.glgames.game;

import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

public class GameObjects {
	public static boolean loaded = false;
	
	public static String[] instructions, help;
	public static BufferedImage logo;
	public static BufferedImage background;

	public static TextBox serverBox, usernameBox;
	public static ServerList serverList;
	public static Rectangle loginButton, helpButton, backButton;

	public static Rectangle playingArea;
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

	public static Texture ice;

	public static void load() {
		try {
			trySleep(500);
			loadingMessage = "Loading text...";
			loadingPercent = 30;
			
			instructions = new String[] { "Push the other players off the ice!",
					"Try not to fall off!" };
			help = new String[] { "Arrow keys - move" };
			logo = SpriteLoader.getSprite("images/logo.png");

			serverBox = new TextBox(IcePush.WIDTH / 2 - 85, 450, false, "Server: ", "localhost");
			usernameBox = new TextBox(IcePush.WIDTH / 2 - 85, 380, true,
					"Username:", "");
			loginButton = new Rectangle(IcePush.WIDTH / 2 - 110, 415, 100, 25);
			helpButton = new Rectangle(IcePush.WIDTH / 2 + 0, 415, 100, 25);
			backButton = new Rectangle(IcePush.WIDTH / 2 - 50, 415, 100, 25);
			
			trySleep(300);
			loadingMessage = "Loading players...";
			loadingPercent = 50;

			if (!IcePush.isApplet)
				serverList = new ServerList(280);
			
			scenery = new Object3D[10];
			scenery[0] = new Object3D.Cube(400);
									
			int width = 400, height = 400;
			int x = IcePush.WIDTH / 2 - width / 2;
			int y = IcePush.HEIGHT / 2 - height / 2 - 45;
			playingArea = new Rectangle(28, 0, 744, 422);

			trySleep(400);
			loadingMessage = "Loading images...";
			loadingPercent = 80;
			
			background = SpriteLoader.getSprite("images/icepush.png");
			
			trySleep(600);
			loadingMessage = "Done";
			loadingPercent = 99; // rounding error and such
			
			trySleep(200);
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
