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
	
	public static void init() {
		frame = new GameFrame();
		new Thread() {
			public void run() {
				GameObjects.load();
			}
		}.start();
	}
	
	public static void run() {
		while(running) {
			gameLoop();
			NetworkHandler.keepAlive();
			NetworkHandler.handlePackets();
			render();
			cycle++;
			try {
				Thread.sleep(20);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void gameLoop() {
		// update positions and such
	}
	
	private static void render() {
		Graphics g = frame.getBufferGraphics();
		if(!GameObjects.loaded) {
			g.setColor(Color.black);
			g.fillRect(0, 0, GameFrame.WIDTH, GameFrame.HEIGHT);
			
			GraphicsMethods.drawLoadingBar(g, GameObjects.loadingMessage,
					GameObjects.loadingPercent);
		} else {
			// clear the screen
			g.setColor(Color.white);
			g.fillRect(0, 0, GameFrame.WIDTH, GameFrame.HEIGHT);
			switch(state) {
				case WELCOME:
					GraphicsMethods.drawWelcomeScreen(g, cycle);
					break;
				case PLAY:
					GraphicsMethods.drawGameBackground(g);
					GraphicsMethods.drawGamePlayers(g);
					GraphicsMethods.drawPlayerStats(g);
					break;
				case DIED:
					if(lastDied == 0)
						lastDied = cycle;
					else if(cycle - lastDied >= 100) {
						lastDied = 0;
						state = PLAY;
					} else
						GraphicsMethods.drawDiedScreen(g, cycle - lastDied);
			}
				
		}
		frame.repaint();
	}

	public static void cleanup() {
		NetworkHandler.logOut();
		frame.dispose();
	}
}
