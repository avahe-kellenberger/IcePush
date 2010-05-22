package com.glgames.game;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;

public class IcePush extends Applet implements Runnable {
	private static final long serialVersionUID = 1L;

	public static boolean DEBUG = false;

	public static IcePush instance;

	// state stuff
	public static final int WELCOME = 0;
	public static final int PLAY = 1;
	public static final int DIED = 2;

	public static int state = WELCOME;

	public static boolean running = true;
	public static transient boolean stable = true;

	public static final int WIDTH = 450;
	public static final int HEIGHT = 600;

	public static GameFrame frame;
	public static Graphics buffGraphics;
	public static int cycle;
	public static int lastDied;

	public static Renderer renderer;

	public static void main(String[] args) {
		_init();
		instance.run();
		cleanup();
	}

	public static void _init() { // AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
		instance = new IcePush();
		if (GameObjects.GRAPHICS_MODE == GameObjects.SOFTWARE_2D)
			renderer = new Renderer2D(instance);
		else
			renderer = new Renderer3D(instance);
		frame = new GameFrame();
		frame.add(instance);
		buffGraphics = renderer.getBufferGraphics();
		//instance.add(renderer.canvas);
		renderer.initGraphics();
		buffGraphics = renderer.bg;
		new Thread() {
			public void run() {
				GameObjects.load();
			}
		}.start();
	}

	public void run() {
		Graphics g = getGraphics();
		while (running) {
			if (!GameObjects.loaded) {
				buffGraphics.setColor(Color.black);
				buffGraphics.fillRect(0, 0, WIDTH, HEIGHT);
				renderer.drawLoadingBar(GameObjects.loadingMessage,
						GameObjects.loadingPercent);
			} else {
				buffGraphics.setColor(Color.black);
				buffGraphics.fillRect(0, 0, WIDTH, HEIGHT);
				if (state == WELCOME) {
					titleLoop();
				} else if (state == PLAY) {
					gameLoop();
				} else if (state == DIED) {
					diedLoop();
				}
			}
			renderer.swapBuffers();
			cycle++;

			try {
				Thread.sleep(20);
			} catch (Exception e) {
				e.printStackTrace();
			}
			g.drawImage(renderer.backbuffer, 0, 0, null);
		}
	}

	private static void titleLoop() {
		renderer.drawWelcomeScreen(cycle);
	}

	private static void gameLoop() {
		// update positions and such
		NetworkHandler.keepAlive();
		NetworkHandler.handlePackets();
		if (!stable)
			return;

		if (GameObjects.GRAPHICS_MODE == GameObjects.SOFTWARE_2D)
			((Renderer2D) renderer)
					.renderScene((Object2D[]) GameObjects.players);
		else
			((Renderer3D) renderer).renderScene(
					(Object3D[]) GameObjects.players,
					(Object3D[]) GameObjects.scenery);
		renderer.drawDebug();
	}

	private static void diedLoop() {
		if (lastDied == 0) {
			lastDied = cycle;
		} else if (cycle - lastDied >= 50) {
			lastDied = 0;
			state = PLAY;
		} else {
			renderer.drawDiedScreen(cycle - lastDied);
		}
	}

	public static void cleanup() {
		NetworkHandler.logOut();
		frame.dispose();
		System.exit(0);
	}

	public Dimension getPreferredSize() {
		return new Dimension(WIDTH, HEIGHT);
	}
}
