package com.glgames.game;

import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

public class GameObjects {
	public static boolean loaded = false;
	
	public static String[] instructions;
	public static BufferedImage logo;
	public static TexturePaint background, foreground;

	public static TextBox serverBox, usernameBox;
	public static Rectangle loginButton;

	public static Rectangle playingArea;
	public static GamePlayer[] players;

	public static String loadingMessage = "Loading title screen...";
	public static int loadingPercent = 10;

	public static void load() {
		try {
			trySleep(500);
			loadingMessage = "Loading text...";
			loadingPercent = 30;
			
			instructions = new String[] { "Push the other players off the ice!",
					"Try not to fall off!" };
			logo = SpriteLoader.getSprite("images/logo.png");

			serverBox = new TextBox(GameFrame.WIDTH / 2 - 85, 450, false, "Server: ", "strictfp.com");
			usernameBox = new TextBox(GameFrame.WIDTH / 2 - 85, 480, true,
					"Username:", "");
			loginButton = new Rectangle(GameFrame.WIDTH / 2 - 50, 520, 100, 25);
			
			trySleep(300);
			loadingMessage = "Loading players...";
			loadingPercent = 50;

			players = new GamePlayer[50];
			int width = 400, height = 400;
			int x = GameFrame.WIDTH / 2 - width / 2;
			int y = GameFrame.HEIGHT / 2 - height / 2 - 45;
			playingArea = new Rectangle(x, y, width, height);

			trySleep(400);
			loadingMessage = "Loading images...";
			loadingPercent = 80;
			
			background = new TexturePaint(SpriteLoader
					.getSprite("images/water.jpg"), new Rectangle(0, 0, 512, 512));
			foreground = new TexturePaint(SpriteLoader.getSprite("images/ice.jpg"),
					new Rectangle(playingArea.x, playingArea.y, 400, 400));
			
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
