package com.glgames.game;

import java.awt.Color;
import java.awt.Graphics;

public class GameEngine {
	// state stuff
	public static final int WELCOME = 0;
	public static final int PLAY = 1;
	public static final int DIED = 2;

	public static int state = WELCOME;

	public static boolean running = true;

	private static GameFrame frame;
	private static Graphics buffGraphics;
	public static int cycle;
	public static int lastDied;

	public static void init() {
		frame = new GameFrame();
		buffGraphics = frame.getRenderer().getBufferGraphics();
		new Thread() {
			public void run() {
				GameObjects.load();
			}
		}.start();
	}

	public static void run() {
		while (running) {
			if (!GameObjects.loaded) {
				buffGraphics.setColor(Color.black);
				buffGraphics.fillRect(0, 0, GameFrame.WIDTH, GameFrame.HEIGHT);
				frame.getRenderer().drawLoadingBar(GameObjects.loadingMessage,
						GameObjects.loadingPercent);
			} else {
				buffGraphics.setColor(Color.white);
				buffGraphics.fillRect(0, 0, GameFrame.WIDTH, GameFrame.HEIGHT);
				if (state == WELCOME) {
					titleLoop();
				} else if (state == PLAY) {
					gameLoop();
				} else if (state == DIED) {
					diedLoop();
				}
			}
			frame.getRenderer().swapBuffers();
			cycle++;

			try {
				Thread.sleep(20);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void titleLoop() {
		frame.getRenderer().drawWelcomeScreen(cycle);
	}

	private static void gameLoop() {
		// update positions and such
		NetworkHandler.keepAlive();
		NetworkHandler.handlePackets();
		frame.getRenderer().renderScene(GameObjects.players);
	}

	private static void diedLoop() {
		if (lastDied == 0) {
			lastDied = cycle;
		} else if (cycle - lastDied >= 50) {
			lastDied = 0;
			state = PLAY;
		} else {
			frame.getRenderer().drawDiedScreen(cycle - lastDied);
		}
	}

	public static void cleanup() {
		NetworkHandler.logOut();
		frame.dispose();
		System.exit(0);
	}
}
