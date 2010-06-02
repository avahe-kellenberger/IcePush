package com.glgames.game;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;

import static java.awt.AWTEvent.*;
import java.awt.event.*;

import javax.swing.Timer;

import com.glgames.server.Player;

import com.glgames.shared.InterthreadQueue;

public class IcePush extends Applet implements Runnable, ActionListener {
	private static final long serialVersionUID = 1L;

	public static boolean DEBUG = true;
	public static IcePush instance;
	public static Renderer renderer;
	public static GameFrame frame;
	public static Graphics buffGraphics;

	// state stuff
	public static final int WELCOME = 0;
	public static final int HELP = 1;
	public static final int PLAY = 2;
	public static final int DIED = 3;

	public static int state = WELCOME;
	
	public static final int WIDTH = 450;
	public static final int HEIGHT = 600;

	public static boolean running = true;
	public static boolean isApplet = false;

	public static int cycle;
	public static int lastDied;

	private InterthreadQueue<TimedKeyEvent> keyEvents;
	private InterthreadQueue<MouseEvent> mouseEvents;

	public static void main(String[] args) {
		_init();
		for (String arg : args) {
			processCommandOption(arg);
		}
		instance.run();
		cleanup();
	}

	private static void processCommandOption(String option) {
		if (option.equalsIgnoreCase("-applet")) {
			instance.init();
		} else if (option.equalsIgnoreCase("-debug")) {
			DEBUG = true;
		}
	}

	public IcePush() {
		enableEvents(MOUSE_EVENT_MASK | KEY_EVENT_MASK);
		keyEvents = new InterthreadQueue<TimedKeyEvent>();
		mouseEvents = new InterthreadQueue<MouseEvent>();
	}

	public void init() {
		// SET ISAPPLET TO TRUE IF AN APPLET
		isApplet = true;
		GameObjects.serverMode = GameObjects.USE_DEFAULT;
		Renderer.message = "Select a username.";
	}

	// --- THIS CODE IS RUN ON THE EVENT DISPATCH THREAD --- //

	protected void processMouseEvent(MouseEvent me) {
		mouseEvents.push(me);
	}

	protected void processKeyEvent(KeyEvent ke) {
		keyEvents.push(new TimedKeyEvent(ke));
	}

	// --- --------------------------------------------- --- //

	private boolean keyPressed = false;

	private void processEvents() {
		TimedKeyEvent tke = null;
		MouseEvent me = null;
		int id;

		while ((tke = keyEvents.pull()) != null) {
			id = tke.event.getID();
			if(id == KeyEvent.KEY_RELEASED) {
				try {
					Thread.sleep(5);			// If this is a spurious released/pressed pair, allow time for the EDT to queue the pressed event
				} catch(Exception e) { }
				TimedKeyEvent tke2 = keyEvents.pull();
				if(tke2 == null) {			// This is the final key release
					keyReleased(tke.event);
				} else if((tke2.time - tke.time) > 1 || tke.event.getID() != KeyEvent.KEY_PRESSED) { // Tke2 is an event that was generated while waiting
					keyReleased(tke.event);
					sendKeyEventInternal(tke2.event);
				}
			} else {
				sendKeyEventInternal(tke.event);
			}
		}

		while ((me = mouseEvents.pull()) != null) {
			id = me.getID();
			if (id == MouseEvent.MOUSE_CLICKED) {
				mouseClicked(me);
			}
		}
	}

	private void sendKeyEventInternal(KeyEvent ke) {
		int id = ke.getID();
		if (id == KeyEvent.KEY_PRESSED) {
			keyPressed(ke);
		} else if (id == KeyEvent.KEY_TYPED) {
			keyTyped(ke);
		} else if (id == KeyEvent.KEY_RELEASED) {
			keyReleased(ke);
		}
	}

	private void mouseClicked(MouseEvent e) {
		if (!GameObjects.loaded)
			return;

		if (IcePush.state == IcePush.WELCOME) {
			if (GameObjects.serverMode == GameObjects.LIST_FROM_SERVER)
				GameObjects.serverList.processClick(e.getX(), e.getY());

			if (GameObjects.loginButton.contains(e.getPoint())) {
				String server = "";
				if (GameObjects.serverMode == GameObjects.LIST_FROM_SERVER)
					server = GameObjects.serverList.getSelected();
				else if (GameObjects.serverMode == GameObjects.TYPE_IN_BOX)
					server = GameObjects.serverBox.getText();
				else if (GameObjects.serverMode == GameObjects.USE_DEFAULT)
					server = "icepush.strictfp.com";
				if (!server.isEmpty()) {
					NetworkHandler.login(server, GameObjects.usernameBox
							.getText());
				}
			} else if (GameObjects.helpButton.contains(e.getPoint())) {
				IcePush.state = IcePush.HELP;
			}
		} else if (IcePush.state == IcePush.HELP) {
			if (GameObjects.backButton.contains(e.getPoint())) {
				IcePush.state = IcePush.WELCOME;
			}
		}
	}

	private void keyPressed(KeyEvent e) {
		if (!GameObjects.loaded)
			return;

		if (IcePush.DEBUG)
			System.out.println("key pressed");

		int moveDir = 0;
		if (IcePush.state == IcePush.WELCOME || IcePush.state == IcePush.HELP) {
			int code = e.getKeyCode();
			if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_TAB) {
				if (GameObjects.serverMode == GameObjects.TYPE_IN_BOX) {
					GameObjects.serverBox.toggleFocused();
					GameObjects.usernameBox.toggleFocused();
				}
			} else if (code == KeyEvent.VK_ESCAPE) {
				IcePush.running = false;
			} else {
				if (GameObjects.serverBox.isFocused())
					GameObjects.serverBox.append(e.getKeyChar());
				else
					GameObjects.usernameBox.append(e.getKeyChar());
			}
		} else
			switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					IcePush.running = false;
					break;
				case KeyEvent.VK_Q:
					NetworkHandler.logOut();
					break;
				case KeyEvent.VK_UP:
					moveDir = Player.UP;
					break;
				case KeyEvent.VK_DOWN:
					moveDir = Player.DOWN;
					break;
				case KeyEvent.VK_LEFT:
					moveDir = Player.LEFT;
					break;
				case KeyEvent.VK_RIGHT:
					moveDir = Player.RIGHT;
					break;
				case KeyEvent.VK_P:
					NetworkHandler.ping();
					break;
			}

		if (moveDir != 0)
			NetworkHandler.sendMoveRequest(moveDir);
	}

	private void keyTyped(KeyEvent ke) {
		// STUB THAT TEKK MIGHT FIND USEFUL TO FIX THE KEYLINUX BUG
	}

	private void keyReleased(KeyEvent e) {
		if (IcePush.DEBUG)
			System.out.println("key released");
		int moveDir = 0;
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				moveDir = Player.UP;
				break;
			case KeyEvent.VK_DOWN:
				moveDir = Player.DOWN;
				break;
			case KeyEvent.VK_LEFT:
				moveDir = Player.LEFT;
				break;
			case KeyEvent.VK_RIGHT:
				moveDir = Player.RIGHT;
				break;
		}
		if (moveDir != 0)
			NetworkHandler.endMoveRequest(moveDir);
	}

	public static void _init() { // AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
		instance = new IcePush();
		instance.setFocusTraversalKeysEnabled(false);
		renderer = new Renderer(instance);
		timer = new Timer(1, instance);
		frame = new GameFrame();
		renderer.initGraphics();
		buffGraphics = renderer.getBufferGraphics();
	}

	public void start() {
		setFocusTraversalKeysEnabled(false);
		renderer = new Renderer(this);
		renderer.initGraphics();
		buffGraphics = renderer.getBufferGraphics();
		timer = new Timer(1, this);
		run();
		cleanup();
	}

	public void run() {
		new Thread() {
			public void run() {
				GameObjects.load();
			}
		}.start();
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
				} else if (state == HELP) {
					helpLoop();
				} else if (state == PLAY) {
					gameLoop();
				} else if (state == DIED) {
					diedLoop();
				}
			}
			renderer.swapBuffers();
			processEvents();
			cycle++;
			try {
				Thread.sleep(20);
			} catch (Exception e) {
				e.printStackTrace();
			}
			g.drawImage(renderer.backbuffer, 0, 0, null);
		}
	}

	public void stop() {
		running = false;
	}

	private static void titleLoop() {
		renderer.drawWelcomeScreen(cycle);
	}

	private static void helpLoop() {
		renderer.drawHelpScreen(cycle);
	}

	private static void gameLoop() {
		// update positions and such
		NetworkHandler.keepAlive();
		NetworkHandler.handlePackets();
		updatePlayers();
		renderer.renderScene(GameObjects.players);
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
		running = false;
		timer.stop();
		timer = null;
		NetworkHandler.logOut();
		instance = null;
		System.gc();
	}

	private static void updatePlayers() {
		for (Player2D p : GameObjects.players) {
			if (p != null)
				p.handleMove();
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(WIDTH, HEIGHT);
	}

	static Timer timer;
	private boolean released;
	private KeyEvent releaseEvent;

	public void actionPerformed(ActionEvent arg0) {
		released = true;
		timer.stop();
		keyReleased(releaseEvent);
	}
}
