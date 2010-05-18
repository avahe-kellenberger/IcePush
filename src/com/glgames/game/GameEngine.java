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
	private static int cycle, lastDied;
	private static Graphics buffGraphics;

	public static void init() {
		frame = new GameFrame();
		buffGraphics = frame.getBufferGraphics();
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
				GraphicsMethods.drawLoadingBar(buffGraphics,
						GameObjects.loadingMessage, GameObjects.loadingPercent);
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
			frame.repaint();
			cycle++;

			try {
				Thread.sleep(20);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void titleLoop() {
		GraphicsMethods.drawWelcomeScreen(buffGraphics, cycle);
	}

	private static void gameLoop() {
		// update positions and such
		NetworkHandler.keepAlive();
		NetworkHandler.handlePackets();
		GraphicsMethods.drawGameBackground(buffGraphics);
		GraphicsMethods.drawGamePlayers(buffGraphics);
		GraphicsMethods.drawPlayerStats(buffGraphics);
	}

	private static void diedLoop() {
		if (lastDied == 0) {
			lastDied = cycle;
		} else if (cycle - lastDied >= 50) {
			lastDied = 0;
			state = PLAY;
		} else {
			GraphicsMethods.drawDiedScreen(buffGraphics, cycle - lastDied);
		}
	}

	public static void cleanup() {
		NetworkHandler.logOut();
		frame.dispose();
		System.exit(0);
	}
}
