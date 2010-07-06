package com.glgames.game;

import static com.glgames.shared.Opcodes.DOWN;
import static com.glgames.shared.Opcodes.LEFT;
import static com.glgames.shared.Opcodes.RIGHT;
import static com.glgames.shared.Opcodes.UP;
import static java.awt.AWTEvent.KEY_EVENT_MASK;
import static java.awt.AWTEvent.MOUSE_EVENT_MASK;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.glgames.shared.InterthreadQueue;

public class IcePush extends Applet {

	public static boolean DEBUG = false;
	
	public static IcePush instance;
	public static Renderer renderer;
	public static GameFrame frame;
	public static Graphics graphics;

	// state stuff
	public static final int WELCOME = 0;
	public static final int HELP = 1;
	public static final int PLAY = 2;
	public static final int DIED = 3;

	public static int state = WELCOME;
	
	public static final int WIDTH = 800;
	public static final int HEIGHT = 480;
	public static transient boolean stable = true;
	public static boolean running = true;

	public static boolean isApplet = false;

	public static int cycle;
	public static int lastDied;

	private InterthreadQueue<TimedKeyEvent> keyEvents;
	private InterthreadQueue<MouseEvent> mouseEvents;

	private int moveKeyFlags;

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

	private void processEvents() {
		TimedKeyEvent tke = null;
		MouseEvent me = null;
		int id;

		while ((tke = keyEvents.pull()) != null) {
			id = tke.event.getID();
			if(id == KeyEvent.KEY_RELEASED) {
				try {
					Thread.sleep(2);			// If this is a spurious released/pressed pair, allow time for the EDT to queue the pressed event
				} catch(Exception e) { }
				TimedKeyEvent tke2 = keyEvents.pull();
				if(tke2 == null) {			// This is the final key release
					System.out.println("final");
					keyReleased(tke.event);
				} else if((tke2.time - tke.time) > 1 || tke.event.getID() != KeyEvent.KEY_PRESSED) { // Tke2 is an event that was generated while waiting
					keyReleased(tke.event); //- not needed, handled below
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
				//System.out.println("serverMode = " + GameObjects.serverMode);
				if (GameObjects.serverMode == GameObjects.LIST_FROM_SERVER) {
					server = GameObjects.serverList.getSelected();
				} else if (GameObjects.serverMode == GameObjects.TYPE_IN_BOX) {
					server = GameObjects.serverBox.getText();
				} else {
					server = "icepush.strictfp.com";
				}
				if (!server.isEmpty()) {
					//System.out.println("Logging in to " + server);
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

	private boolean is_chat = false;
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
				if(!isApplet) cleanup();
			} else {
				if (GameObjects.serverBox.isFocused())
					GameObjects.serverBox.append(e.getKeyChar());
				else
					GameObjects.usernameBox.append(e.getKeyChar());
			}
		} else if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			is_chat = !is_chat;
			if(is_chat)
				Renderer.curChat = "";
			else if (!is_chat && !Renderer.curChat.isEmpty()) {
				NetworkHandler.sendChatMessage(Renderer.curChat.replace("\r\n", "").replace("\n", ""));
				Renderer.curChat = "<enter> to chat";
			}
		} else if(!is_chat)
			switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					if(!isApplet) cleanup();
					break;
				case KeyEvent.VK_Q:
					NetworkHandler.logOut();
					if(GameObjects.serverList != null) GameObjects.serverList.interrupt();
					break;
				case KeyEvent.VK_UP:
					moveDir = UP;
					break;
				case KeyEvent.VK_DOWN:
					moveDir = DOWN;
					break;
				case KeyEvent.VK_LEFT:
					moveDir = LEFT;
					break;
				case KeyEvent.VK_RIGHT:
					moveDir = RIGHT;
					break;
				case KeyEvent.VK_P:
					NetworkHandler.ping();
					break;
				case KeyEvent.VK_W:
					if (Renderer.GRAPHICS_MODE == Renderer.SOFTWARE_3D)
						renderer.pitch -= 5;
					break;
				case KeyEvent.VK_S:
					if (Renderer.GRAPHICS_MODE == Renderer.SOFTWARE_3D)
						renderer.pitch += 5;
					break;
				case KeyEvent.VK_A:
					if (Renderer.GRAPHICS_MODE == Renderer.SOFTWARE_3D)
						renderer.yaw -= 5;
					break;
				case KeyEvent.VK_D:
					if (Renderer.GRAPHICS_MODE == Renderer.SOFTWARE_3D)
						renderer.yaw += 5;
					break;
				case KeyEvent.VK_2:
					Renderer.GRAPHICS_MODE = Renderer.SOFTWARE_2D;
					break;
				case KeyEvent.VK_3:
					Renderer.GRAPHICS_MODE = Renderer.SOFTWARE_3D;
					break;
				case KeyEvent.VK_J:
					renderer.cameraX -= 5;
					break;
				case KeyEvent.VK_L:
					renderer.cameraX += 5;
					break;
				case KeyEvent.VK_M:
					renderer.cameraY -= 5;
					break;
				case KeyEvent.VK_I:
					renderer.cameraY += 5;
					break;
			}

		if (moveDir != 0) {
			if((moveKeyFlags | moveDir) != moveKeyFlags) {
				moveKeyFlags |= moveDir;
				NetworkHandler.sendMoveRequest(moveDir);
			}
		}
	}

	private void keyTyped(KeyEvent e) {
		if(is_chat) {
			char c = e.getKeyChar();
			if (c == 8 && Renderer.curChat.length() > 0)
				Renderer.curChat = Renderer.curChat.substring(0,
						Renderer.curChat.length() - 1);
			else
				Renderer.curChat += c;
		}
	}

	private void keyReleased(KeyEvent e) {
		if (IcePush.DEBUG)
			System.out.println("key released");
		int moveDir = 0;
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				moveDir = UP;
				break;
			case KeyEvent.VK_DOWN:
				moveDir = DOWN;
				break;
			case KeyEvent.VK_LEFT:
				moveDir = LEFT;
				break;
			case KeyEvent.VK_RIGHT:
				moveDir = RIGHT;
				break;
		}
		if (moveDir != 0) {
			moveKeyFlags &= (~moveDir);
			NetworkHandler.endMoveRequest(moveDir);
		}
	}

	public static void _init() { // AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
		instance = new IcePush();
		instance.setFocusTraversalKeysEnabled(false);
		renderer = new Renderer(instance);
		frame = new GameFrame();
		renderer.initGraphics();
		graphics = renderer.getBufferGraphics();
	}

	public void start() {
		setFocusTraversalKeysEnabled(false);
		renderer = new Renderer(this);
		renderer.initGraphics();
		graphics = renderer.getBufferGraphics();
		run();
		cleanup();
	}

	public void run() {
		new Thread() {
			public void run() {
				GameObjects.load();
			}
		}.start();
		while (running) {
			if (!GameObjects.loaded) {
				graphics.setColor(Color.black);
				graphics.fillRect(0, 0, WIDTH, HEIGHT);
				renderer.drawLoadingBar(GameObjects.loadingMessage,
						GameObjects.loadingPercent);
			} else {
				graphics.setColor(Color.black);
				graphics.fillRect(0, 0, WIDTH, HEIGHT);
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
			Graphics g = getGraphics();
			if(g != null && renderer.backbuffer != null)
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
		renderer.renderScene();
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
	}

	private static void updatePlayers() {
		
	}

	public Dimension getPreferredSize() {
		return new Dimension(WIDTH, HEIGHT);
	}
}
