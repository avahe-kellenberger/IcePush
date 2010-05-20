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
	public static transient boolean stable = true;

	public static GameFrame frame;
	private static Graphics buffGraphics;
	public static int cycle;
	public static int lastDied;

	public static void init() {
		frame = new GameFrame();
		buffGraphics = frame.renderer.getBufferGraphics();
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
				frame.renderer.drawLoadingBar(GameObjects.loadingMessage,
						GameObjects.loadingPercent);
			} else {
				buffGraphics.setColor(Color.black);
				buffGraphics.fillRect(0, 0, GameFrame.WIDTH, GameFrame.HEIGHT);
				if (state == WELCOME) {
					titleLoop();
				} else if (state == PLAY) {
					gameLoop();
				} else if (state == DIED) {
					diedLoop();
				}
			}
			frame.renderer.drawDebug();
			frame.renderer.swapBuffers();
			cycle++;

			try {
				Thread.sleep(20);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void titleLoop() {
		frame.renderer.drawWelcomeScreen(cycle);
	}

	private static void gameLoop() {
		// update positions and such
		NetworkHandler.keepAlive();
		NetworkHandler.handlePackets();
		if(!stable)
			return;
		
		if (GameObjects.GRAPHICS_MODE == GameObjects.TWO_D)
			((Renderer2D) frame.renderer)
					.renderScene((Object2D[]) GameObjects.players);
		else
			((Renderer3D) frame.renderer)
					.renderScene((Object3D[]) GameObjects.players);
	}

	private static void diedLoop() {
		if (lastDied == 0) {
			lastDied = cycle;
		} else if (cycle - lastDied >= 50) {
			lastDied = 0;
			state = PLAY;
		} else {
			frame.renderer.drawDiedScreen(cycle - lastDied);
		}
	}

	public static void cleanup() {
		NetworkHandler.logOut();
		frame.dispose();
		System.exit(0);
	}
}
