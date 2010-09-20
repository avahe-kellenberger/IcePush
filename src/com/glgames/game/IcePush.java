package com.glgames.game;

import static com.glgames.shared.Opcodes.DOWN;
import static com.glgames.shared.Opcodes.LEFT;
import static com.glgames.shared.Opcodes.RIGHT;
import static com.glgames.shared.Opcodes.UP;
import static java.awt.AWTEvent.KEY_EVENT_MASK;
import static java.awt.AWTEvent.MOUSE_EVENT_MASK;
import static java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.glgames.graphics2d.Renderer;
import com.glgames.shared.InterthreadQueue;
import com.glgames.ui.Action;
import com.glgames.ui.Actions;
import com.glgames.ui.Button;
import com.glgames.ui.MapCanvas;
import com.glgames.ui.TextBox;

public class IcePush extends Applet {

	public static boolean DEBUG = false;

	public static IcePush instance;
	public static ClientRenderer renderer;
	public static GameFrame frame;

	// state stuff
	public static final int NONE = 0;
	public static final int WELCOME = 1 << 0;
	public static final int HELP = 1 << 1;
	public static final int PLAY = 1 << 2;
	public static final int MAPEDITOR = 1 << 3;
	public static int state = WELCOME;

	public static final int WIDTH = 800;
	public static final int HEIGHT = 480;
	public static transient boolean stable = true;
	public static boolean running = true;

	public static boolean isApplet = false;
	public static boolean is_chat = false;

	private InterthreadQueue<TimedKeyEvent> keyEvents;
	private InterthreadQueue<MouseEvent> mouseEvents;

	private int moveKeyFlags;

	public static String username;

	public static void main(String[] args) {
		_init();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-applet")) {
				instance.init();
			} else if (args[i].equalsIgnoreCase("-debug")) {
				DEBUG = true;
			} else if(args[i].equalsIgnoreCase("-server")) {
				if(i + 1 == args.length) {
					System.out.println("No server argument provided to -server option");
					return;
				}
				NetworkHandler.DEFAULT_SERVER = args[i + 1];
				i++;
			} else if(args[i].equalsIgnoreCase("-username")) {
				if(i + 1 == args.length) {
					System.out.println("No username argument provided to -username option");
					return;
				}
				username = args[i + 1];
			}
		}
		GameObjects.load();
		instance.run();
		cleanup();
	}

	public IcePush() {
		enableEvents(MOUSE_EVENT_MASK | MOUSE_MOTION_EVENT_MASK
				| KEY_EVENT_MASK);
		keyEvents = new InterthreadQueue<TimedKeyEvent>();
		mouseEvents = new InterthreadQueue<MouseEvent>();
	}

	public void init() {
		// SET ISAPPLET TO TRUE IF AN APPLET
		isApplet = true;
		String s = getParameter("server");
		String u = getParameter("username");
		if(s != null) NetworkHandler.DEFAULT_SERVER = s;
		if(u != null) username = u;
		GameObjects.serverMode = GameObjects.USE_DEFAULT;
	}

	public static void _init() {
		instance = new IcePush();
		instance.setFocusTraversalKeysEnabled(false);
		renderer = new ClientRenderer(instance, WIDTH, HEIGHT);
		frame = new GameFrame();
		renderer.initGraphics();
	}

	public void start() {
		setFocusTraversalKeysEnabled(false);
		renderer = new ClientRenderer(this, WIDTH, HEIGHT);
		renderer.initGraphics();
		GameObjects.load();
		run();
		cleanup();
	}

	public void run() {
		Thread painter = new Thread() {
			public void run() {
				while (running) {
					repaint();
					try {
						Thread.sleep(20);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		painter.start();

		while (running) {
			if (state == PLAY) {
				gameLoop();
			}
			processEvents();
			try {
				Thread.sleep(20);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void stop() {
		running = false;
	}

	public static void cleanup() {
		running = false;
		NetworkHandler.logOut();
		instance = null;
		System.gc();
	}

	private static void gameLoop() {
		// update positions and such
		NetworkHandler.handlePackets();
		updatePlayers();
	}

	public void paint(Graphics g) {
		if (!GameObjects.loaded)
			return;

		Graphics bg = renderer.getBufferGraphics();
		if ((state == WELCOME) || (state == HELP) || (state == MAPEDITOR)) {
			renderer.drawWelcomeScreen(bg);
		} else if (state == PLAY) {
			renderer.renderScene(bg);
		}
		GameObjects.ui.draw(bg);
		g.drawImage(renderer.getBuffer(), 0, 0, this);
	}
	
	public void update(Graphics g) {
		paint(g);
	}

	public static Action<Button> onHelpButtonClick = new Action<Button>() {
		public void doAction(Button component, int x, int y) {
			IcePush.state = IcePush.HELP;
			GameObjects.ui.setVisibleRecursive(false);
			GameObjects.ui.setVisible(true);
			GameObjects.ui.helpScreenContainer.setVisibleRecursive(true);
		}
	};
	
	public static Action<Button> onMapEditorButtonClick = new Action<Button>() {
		public void doAction(Button component, int x, int y) {
			IcePush.state = IcePush.MAPEDITOR;
			GameObjects.ui.setVisibleRecursive(false);
			GameObjects.ui.setVisible(true);
			GameObjects.ui.mapEditorScreenContainer.setVisibleRecursive(true);
		}
	};

	public static Action<Button> onBackButtonClick = new Action<Button>() {
		public void doAction(Button component, int x, int y) {
			IcePush.state = IcePush.WELCOME;
			GameObjects.ui.setVisibleRecursive(false);
			GameObjects.ui.setVisible(true);
			GameObjects.ui.welcomeScreenContainer.setVisibleRecursive(true);
			if (GameObjects.serverMode == GameObjects.LIST_FROM_SERVER) {
				GameObjects.ui.serverTextBox.setVisible(false);
			} else {
				//GameObjects.ui.serverList.setVisible(false);
			}
		}
	};

	public static Action<TextBox> onUsernameTextBoxClick = new Action<TextBox>() {
		public void doAction(TextBox component, int x, int y) {
			GameObjects.ui.serverTextBox.unfocus();
			GameObjects.ui.usernameTextBox.focus();
		}
	};

	public static Action<TextBox> onServerTextBoxClick = new Action<TextBox>() {
		public void doAction(TextBox component, int x, int y) {
			GameObjects.ui.usernameTextBox.unfocus();
			GameObjects.ui.serverTextBox.focus();
		}
	};

	public static Action<Button> onSelectButtonClick = new Action<Button>() {
		public void doAction(Button component, int x, int y) {
			GameObjects.ui.mapCanvas.setTool(MapCanvas.Tool.SELECT);
		}
	};

	public static Action<Button> onLineButtonClick = new Action<Button>() {
		public void doAction(Button component, int x, int y) {
			GameObjects.ui.mapCanvas.setTool(MapCanvas.Tool.LINE);
		}
	};
	
	public static Action<Button> onQuadButtonClick = new Action<Button>() {
		public void doAction(Button component, int x, int y) {
			GameObjects.ui.mapCanvas.setTool(MapCanvas.Tool.QUADRATIC);
		}
	};

	public static Action<Button> onCubicButtonClick = new Action<Button>() {
		public void doAction(Button component, int x, int y) {
			GameObjects.ui.mapCanvas.setTool(MapCanvas.Tool.CUBIC);
		}
	};

	public static Action<Button> onCloseButtonClick = new Action<Button>() {
		public void doAction(Button component, int x, int y) {
			GameObjects.ui.mapCanvas.closePath();
		}
	};

	public static Action<Button> onExportButtonClick = new Action<Button>() {
		public void doAction(Button component, int x, int y) {
			GameObjects.ui.mapCanvas.exportPath();
		}
	};

	public static Action<Button> onImportButtonClick = new Action<Button>() {
		public void doAction(Button component, int x, int y) {
			GameObjects.ui.mapCanvas.importPath();
		}
	};

	/*public static Action<ServerList> onServerListClick = new Action<ServerList>() {
		public void doAction(ServerList component, int x, int y) {
			if (IcePush.state != IcePush.WELCOME 
					|| GameObjects.serverMode != GameObjects.LIST_FROM_SERVER)
				return;
			int index = y / component.getFontHeight();
			if(IcePush.DEBUG)
				System.out.println(index);
			synchronized(component) {
				if(index < 0 || index > component.getItems().length - 1)
					return;
				component.setSelected(index);
			}
		}
	};*/

	// --- THIS CODE IS RUN ON THE EVENT DISPATCH THREAD --- //

	protected void processMouseEvent(MouseEvent me) {
		mouseEvents.push(me);
	}

	protected void processMouseMotionEvent(MouseEvent me) {
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
			if (id == KeyEvent.KEY_RELEASED) {
				try {
					Thread.sleep(2); // If this is a spurious released/pressed
										// pair, allow time for the EDT to queue
										// the pressed event
				} catch (Exception e) {
				}
				TimedKeyEvent tke2 = keyEvents.pull();
				if (tke2 == null) { // This is the final key release
					// System.out.println("final");
					keyReleased(tke.event);
				} else if ((tke2.time - tke.time) > 1
						|| tke.event.getID() != KeyEvent.KEY_PRESSED) {
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
			} else if (id == MouseEvent.MOUSE_PRESSED) {
				mousePressed(me);
			} else if (id == MouseEvent.MOUSE_RELEASED) {
				mouseReleased(me);
			} else if (id == MouseEvent.MOUSE_MOVED) {
				mouseMoved(me);
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

	private void mousePressed(MouseEvent e) {
		if (DEBUG)
			System.out.println("Pressed");
		int x = e.getX();
		int y = e.getY();
		GameObjects.ui.handleAction(Actions.PRESS, x, y);
	}

	private void mouseReleased(MouseEvent e) {
		if (DEBUG)
			System.out.println("Released");
		int x = e.getX();
		int y = e.getY();
		GameObjects.ui.handleAction(Actions.RELEASE, x, y);
	}

	private void mouseClicked(MouseEvent e) {
		if (DEBUG)
			System.out.println("Clicked");
		int x = e.getX();
		int y = e.getY();
		GameObjects.ui.handleAction(Actions.CLICK, x, y);
	}

	private void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		GameObjects.ui.handleAction(Actions.HOVER, x, y);
	}

	private void keyPressed(KeyEvent e) {
		if (IcePush.DEBUG)
			System.out.println("key pressed");

		int moveDir = 0;
		if (IcePush.state == IcePush.WELCOME || IcePush.state == IcePush.HELP) {
			int code = e.getKeyCode();
			if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_TAB) {
				if (GameObjects.serverMode == GameObjects.TYPE_IN_BOX) {
					GameObjects.ui.serverTextBox.toggleFocus();
					GameObjects.ui.usernameTextBox.toggleFocus();
				}
			} else if (code == KeyEvent.VK_ESCAPE) {
				if (!isApplet)
					cleanup();
			} else {
				if ((GameObjects.ui.serverTextBox.hasFocus()) && (GameObjects.ui.serverTextBox.getVisible()))
					GameObjects.ui.serverTextBox.append(e.getKeyChar());
				else if (GameObjects.ui.usernameTextBox.getVisible())
					GameObjects.ui.usernameTextBox.append(e.getKeyChar());
			}
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			is_chat = !is_chat;
			if (!is_chat && !Renderer.curChat.trim().isEmpty()) {
				NetworkHandler.sendChatMessage(Renderer.curChat.trim());
				Renderer.curChat = "";
			}
		} else if (!is_chat)
			switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					if (!isApplet)
						cleanup();
					break;
				case KeyEvent.VK_Q:
					NetworkHandler.onLogoutButtonClick.doAction(GameObjects.ui.logoutButton, 0, 0);
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
				case KeyEvent.VK_C:
					Renderer.chats_visible = !Renderer.chats_visible;
					break;
				case KeyEvent.VK_W:
					if (ClientRenderer.GRAPHICS_MODE == ClientRenderer.SOFTWARE_3D)
						renderer.pitch -= 5;
					break;
				case KeyEvent.VK_S:
					if (ClientRenderer.GRAPHICS_MODE == ClientRenderer.SOFTWARE_3D)
						renderer.pitch += 5;
					break;
				case KeyEvent.VK_A:
					if (ClientRenderer.GRAPHICS_MODE == ClientRenderer.SOFTWARE_3D)
						renderer.yaw -= 5;
					break;
				case KeyEvent.VK_D:
					if (ClientRenderer.GRAPHICS_MODE == ClientRenderer.SOFTWARE_3D)
						renderer.yaw += 5;
					break;
				case KeyEvent.VK_2:
					ClientRenderer.GRAPHICS_MODE = ClientRenderer.SOFTWARE_2D;
					break;
				case KeyEvent.VK_3:
					ClientRenderer.GRAPHICS_MODE = ClientRenderer.SOFTWARE_3D;
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
			if ((moveKeyFlags | moveDir) != moveKeyFlags) {
				moveKeyFlags |= moveDir;
				NetworkHandler.sendMoveRequest(moveDir);
			}
		}
	}

	private void keyTyped(KeyEvent e) {
		if (is_chat) {
			char c = e.getKeyChar();
			if (c == 8 && Renderer.curChat.length() > 0)
				Renderer.curChat = Renderer.curChat.substring(0,
						Renderer.curChat.length() - 1);
			else if (c >= ' ')
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

	private static void updatePlayers() {

	}

	public Dimension getPreferredSize() {
		return new Dimension(WIDTH, HEIGHT);
	}
}
