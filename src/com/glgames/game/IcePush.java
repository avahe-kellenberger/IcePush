package com.glgames.game;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;

import static java.awt.AWTEvent.*;
import java.awt.event.*;

import com.glgames.server.Player;

import com.glgames.shared.InterthreadQueue;

public class IcePush extends Applet implements Runnable {
	private static final long serialVersionUID = 1L;

	public static boolean DEBUG = false;

	public static IcePush instance;

	// state stuff
	public static final int WELCOME = 0;
	public static final int HELP = 1;
	public static final int PLAY = 2;
	public static final int DIED = 3;

	public static int state = WELCOME;

	public static boolean running = true;
	public static transient boolean stable = true;

	public static final int WIDTH = 450;
	public static final int HEIGHT = 600;

	public static GameFrame frame;
	public static Graphics buffGraphics;
	public static int cycle;
	public static int lastDied;

	static int moveFlags;

	public static Renderer renderer;

	public static boolean isApplet = false;

	private InterthreadQueue<KeyEvent> keyEvents;
	private InterthreadQueue<MouseEvent> mouseEvents;

	public static void main(String[] args) {
		_init();
		for (String arg : args) { processCommandOption(arg); }
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
		keyEvents = new InterthreadQueue<KeyEvent>();
		mouseEvents = new InterthreadQueue<MouseEvent>();
	}

	public void init() {
		// SET ISAPPLET TO TRUE IF AN APPLET
		isApplet = true;
		GameObjects.serverMode = GameObjects.USE_DEFAULT;
		Renderer.message = "Select a username.";
	}

	public void processMouseEvent(MouseEvent me) {
		mouseEvents.push(me);
		super.processMouseEvent(me);
	}

	public void processKeyEvent(KeyEvent ke) {
		keyEvents.push(ke);
		super.processKeyEvent(ke);
	}

	private void processEvents() {
		KeyEvent ke = null;
		MouseEvent me = null;
		int id;

		while((ke = keyEvents.pull()) != null) {
			id = ke.getID();
			if(id == KeyEvent.KEY_PRESSED) {
				keyPressed(ke);
			} else if(id == KeyEvent.KEY_TYPED) {
				keyTyped(ke);
			} else if(id == KeyEvent.KEY_RELEASED) {
				keyReleased(ke);
			}
		}

		while((me = mouseEvents.pull()) != null) {
			id = me.getID();
			if(id == MouseEvent.MOUSE_CLICKED) {
				mouseClicked(me);
			}
		}
	}

	private void mouseClicked(MouseEvent e) {
		if (!GameObjects.loaded)
			return;

		if(IcePush.state == IcePush.WELCOME) {
			if(GameObjects.serverMode == GameObjects.LIST_FROM_SERVER)
				GameObjects.serverList.processClick(e.getX(), e.getY());
			
			if (GameObjects.loginButton.contains(e.getPoint())) {
				String server = "";
				if (GameObjects.serverMode == GameObjects.LIST_FROM_SERVER) {
					server = GameObjects.serverList.getSelected();
				} else if(GameObjects.serverMode == GameObjects.TYPE_IN_BOX) {
					server = GameObjects.serverBox.getText();
				} else if(GameObjects.serverMode == GameObjects.USE_DEFAULT) {
					server = "icepush.strictfp.com"; // getCodeBase();
				}
				if (!server.isEmpty()) {
				//	new Exception("LOGGING IN NOW: " + server + "GAMESERVERTYPE " + GameObjects.serverMode).printStackTrace();
					NetworkHandler.login(server, GameObjects.usernameBox.getText());
				}
			} else if (GameObjects.helpButton.contains(e.getPoint())) {
				IcePush.state = IcePush.HELP;
			}
		} else if(IcePush.state == IcePush.HELP) {
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
				case KeyEvent.VK_W:
					if (GameObjects.GRAPHICS_MODE == GameObjects.SOFTWARE_3D)
						((Renderer3D) IcePush.renderer).pitch -= 5;
					break;
				case KeyEvent.VK_S:
					if (GameObjects.GRAPHICS_MODE == GameObjects.SOFTWARE_3D)
						((Renderer3D) IcePush.renderer).pitch += 5;
					break;
				case KeyEvent.VK_A:
					if (GameObjects.GRAPHICS_MODE == GameObjects.SOFTWARE_3D)
						((Renderer3D) IcePush.renderer).yaw -= 5;
					break;
				case KeyEvent.VK_D:
					if (GameObjects.GRAPHICS_MODE == GameObjects.SOFTWARE_3D)
						((Renderer3D) IcePush.renderer).yaw += 5;
					break;
				case KeyEvent.VK_2:
					IcePush.renderer.switchMode(GameObjects.SOFTWARE_2D);
					break;
				case KeyEvent.VK_3:
					IcePush.renderer.switchMode(GameObjects.SOFTWARE_3D);
					break;
			}

		if (moveDir != 0) {
			if (isSet(moveDir))
				return;
			NetworkHandler.sendMoveRequest(moveDir);
			setBit(moveDir);
		}
	}

	private void keyTyped(KeyEvent ke) {
		// STUB THAT TEKK MIGHT FIND USEFUL TO FIX THE KEYLINUX BUG
	}

	private void keyReleased(KeyEvent e) {
	//	if (!getReleased())
	//		return;
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
		if (moveDir != 0) {
			NetworkHandler.endMoveRequest(moveDir);
			clearBit(moveDir);
		}
	}

	private void setBit(int flag) {
		moveFlags |= flag;
	}

	private void clearBit(int flag) {
		moveFlags &= ~flag;
	}

	private boolean isSet(int flag) {
		return (moveFlags & flag) > 0;
	}

	public static void _init() { // AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
		instance = new IcePush();
		instance.setFocusTraversalKeysEnabled(false);
		if (GameObjects.GRAPHICS_MODE == GameObjects.SOFTWARE_2D)
			renderer = new Renderer2D(instance);
		else
			renderer = new Renderer3D(instance);
		
		frame = new GameFrame();
		renderer.initGraphics();
		buffGraphics = renderer.getBufferGraphics();
	}
	
	public static void setRenderer(Renderer r) {
		renderer = r;
		renderer.initGraphics();
		buffGraphics = renderer.getBufferGraphics();
	}
	
	public void start() {
		setFocusTraversalKeysEnabled(false);
		if (GameObjects.GRAPHICS_MODE == GameObjects.SOFTWARE_2D)
			renderer = new Renderer2D(this);
		else
			renderer = new Renderer3D(this);
		
		renderer.initGraphics();
		buffGraphics = renderer.getBufferGraphics();
		
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
		if (!stable)
			return;

		if (renderer instanceof Renderer2D)
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
		running = false;
		NetworkHandler.logOut();
		instance = null;
		System.gc();
	//	if(!anApplet) System.exit(0); // -- TEMPORARY SOLUTION FOR TIMER FIRING FAILURE FAGGOTRY //
	}

	public Dimension getPreferredSize() {
		return new Dimension(WIDTH, HEIGHT);
	}
}
