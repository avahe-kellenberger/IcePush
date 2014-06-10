package net.threesided.game;

import static java.awt.AWTEvent.KEY_EVENT_MASK;
import static java.awt.AWTEvent.MOUSE_EVENT_MASK;
import static java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import net.threesided.graphics2d.Renderer;
import net.threesided.ui.Action;
import net.threesided.ui.Actions;
import net.threesided.ui.MapCanvas;
import net.threesided.ui.UIComponent;

import net.threesided.shared.PacketBuffer;
import static net.threesided.shared.Constants.*;
import net.threesided.shared.ILoader;

public class IcePush extends Applet {
    public static boolean DEBUG = false;

    public static IcePush instance;
    public static ClientRenderer renderer;
    public static GameFrame frame;

    // state stuff
    //public static final int NONE = 0;
    public static final int WELCOME = 1;
    public static final int HELP = 1 << 1;
    public static final int PLAY = 1 << 2;
    public static final int MAPEDITOR = 1 << 3;
    public static int state = WELCOME;

    public static final int WIDTH = 800;
    public static final int HEIGHT = 480;
    public static boolean running = true;

    public static boolean isApplet = false;
    public static boolean in_chat = false;

    private static boolean[] keys = new boolean[256], previous = new boolean[256];
    private static int angle = -1;
    private static int prev_angle = -1;

    public static String username;

    public static void main(String[] args) {
        _init();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-applet")) {
                instance.init();
            } else if (args[i].equalsIgnoreCase("-debug")) {
                DEBUG = true;
		NetworkHandler.DEBUG = true;
            } else if (args[i].equalsIgnoreCase("-server")) {
                if (i + 1 == args.length) {
                    System.out.println("No server argument provided to -server option");
                    return;
                }
                NetworkHandler.DEFAULT_SERVER = args[i + 1];
                i++;
            } else if (args[i].equalsIgnoreCase("-username")) {
                if (i + 1 == args.length) {
                    System.out.println("No username argument provided to -username option");
                    return;
                }
                username = args[i + 1];
            }
        }
        GameObjects.load(WIDTH, HEIGHT);
	instance.initClientUI();
        instance.run();
        cleanup();
    }

    public IcePush() {
        enableEvents(MOUSE_EVENT_MASK | MOUSE_MOTION_EVENT_MASK
                | KEY_EVENT_MASK);
    }

    public void init() {
        // SET ISAPPLET TO TRUE IF AN APPLET
        isApplet = true;
        String s = getParameter("server");
        String u = getParameter("username");
        if (s != null) NetworkHandler.DEFAULT_SERVER = s;
        else NetworkHandler.DEFAULT_SERVER = getCodeBase().getHost();
        if (u != null) username = u;
        GameObjects.serverMode = GameObjects.USE_DEFAULT;
    }

    public static void _init() {
        instance = new IcePush();
        instance.setFocusTraversalKeysEnabled(false);
        renderer = new ClientRenderer(instance, WIDTH, HEIGHT);
        frame = new GameFrame("IcePush", instance);
        renderer.initGraphics();
    }

    public void start() {
        setFocusTraversalKeysEnabled(false);
        renderer = new ClientRenderer(this, WIDTH, HEIGHT);
        renderer.initGraphics();
        GameObjects.load(WIDTH, HEIGHT);
	initClientUI();
        run();
        cleanup();
    }

	private void initClientUI() {
		GameObjects.ui.serverTextBox.setText(NetworkHandler.DEFAULT_SERVER);
		if(username != null) GameObjects.ui.usernameTextBox.setText(username);
		GameObjects.ui.loginButton.setClickAction(onLoginButtonClick);
		GameObjects.ui.logoutButton.setClickAction(onLogoutButtonClick);
		GameObjects.ui.helpButton.setClickAction(onHelpButtonClick);
		GameObjects.ui.mapEditorButton.setClickAction(onMapEditorButtonClick);
		GameObjects.ui.backButton.setClickAction(onBackButtonClick);
	}

    public void run() {
        while (running) {
            long start = System.nanoTime();
            if (state == PLAY) {
                gameLoop();
            }
            Graphics bg = renderer.getBufferGraphics();
            if (state == WELCOME || state == HELP || state == MAPEDITOR) {
                renderer.drawWelcomeScreen(bg);
            } else if (state == PLAY) {
                renderer.renderScene(bg, in_chat);
            }
            GameObjects.ui.draw(bg);
            getGraphics().drawImage(renderer.getBuffer(), 0, 0, null);
            long end = 20 - ((System.nanoTime() - start) / 1000000L);
            try {
                if (end > 0)
                    Thread.sleep(end);
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
	GameFrame fr = frame;
	if(fr != null) fr.dispose();
        instance = null;
        System.gc();
    }

	public void removeNotify() {
		super.removeNotify();
		stop();
	}

    private void gameLoop() {
        // update positions and such
        handlePackets();
        checkKeys();
        if (angle != prev_angle) {
		boolean in2dmode = ClientRenderer.GRAPHICS_MODE == ClientRenderer.SOFTWARE_2D;
            if (prev_angle != -1)
                NetworkHandler.move(in2dmode ? prev_angle : prev_angle + renderer.yaw, true);
            if (angle != -1)
                NetworkHandler.move(in2dmode ? angle : angle + renderer.yaw, false);
        }
        prev_angle = angle;
        angle = -1;
    }

	private void handlePackets() {
		//if (IcePush.state == IcePush.WELCOME)
		//	return;

		if (!NetworkHandler.pbuf.synch()) {
			state = WELCOME;
			GameObjects.ui.networkStatus.setText("Connection with server was lost.");
			GameObjects.ui.setVisibleRecursive(false);
			GameObjects.ui.setVisible(true);
			GameObjects.ui.welcomeScreenContainer.setVisibleRecursive(true);
			return;
		}

		int opcode;
		int id, type, x, y;
		String username;
		Player plr;
		PacketBuffer pbuf = NetworkHandler.pbuf;

		while ((opcode = pbuf.openPacket()) != -1) {
			switch (opcode) {
				case NEW_PLAYER:
					id = pbuf.readShort();
					type = pbuf.readByte(); // snowman or tree??
					username = pbuf.readString();
					int deaths = pbuf.readShort();
					plr = new Player(type, username);
					plr.username = username;
					plr.deaths = deaths;
					plr.isDead = true;
					GameObjects.players[id] = plr;
					break;
				case PLAYER_MOVED:
					id = pbuf.readShort(); // player ID
					x = pbuf.readShort();
					y = pbuf.readShort();
					plr = GameObjects.players[id];
					if (plr == null) {
						System.out.println("null player tried to move??? " + id);
						break;
					}
					plr.isDead = false;
					plr.setPos(x, y);
					if (id == NetworkHandler.id)
						IcePush.renderer.updateCamera(x, y);
					break;
				case PLAYER_DIED:
					id = pbuf.readShort();
					plr = GameObjects.players[id];
					if (plr == null)
						break;
					plr.deaths = pbuf.readByte();
					if (plr.deaths != 0) // death reset, not dead
						plr.isDead = true;
					break;
				case PLAYER_LOGGED_OUT:
					id = pbuf.readShort();
					GameObjects.players[id] = null;
				case KEEP_ALIVE:
					break;
				case PING:
					System.out.println("Ping response recieved: "
							+ (System.currentTimeMillis() - NetworkHandler.pingTime));
					break;
				case NEW_CHAT_MESSAGE:
					String msg = pbuf.readString();
					Renderer.chats.add(msg);
					break;
				case UPDATE:
					try {
						Class<?> clazz = NetworkHandler.class.getClassLoader()
								.loadClass("loader");
						ILoader loader = (ILoader) clazz.newInstance();
						loader.restart();
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case UPDATE_TIME:
					int time_type = pbuf.readByte();
					int time = pbuf.readShort();
					switch (time_type) {
						case 1:
							IcePush.renderer.setRoundTime(time);
							break;
						case 2:
							IcePush.renderer.setDeathTime(time);
					}
					break;
			}
			pbuf.closePacket();
		}
	}

    public void paint(Graphics g) {

    }

    public void update(Graphics g) {
        paint(g);
    }

    public static Action onHelpButtonClick = new Action() {
        public void doAction(UIComponent uiComp, int x, int y) {
            IcePush.state = IcePush.HELP;
            GameObjects.ui.setVisibleRecursive(false);
            GameObjects.ui.setVisible(true);
            GameObjects.ui.helpScreenContainer.setVisibleRecursive(true);
        }
    };

    public static Action onMapEditorButtonClick = new Action() {
        public void doAction(UIComponent uiComp, int x, int y) {
            IcePush.state = IcePush.MAPEDITOR;
            GameObjects.ui.setVisibleRecursive(false);
            GameObjects.ui.setVisible(true);
            GameObjects.ui.mapEditorScreenContainer.setVisibleRecursive(true);
        }
    };

    public static Action onBackButtonClick = new Action() {
        public void doAction(UIComponent uiComp, int x, int y) {
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

	public static Action onLoginButtonClick = new Action() {
		public void doAction(UIComponent uiComp, int x, int y) {
			String server;
			if (GameObjects.serverMode == GameObjects.TYPE_IN_BOX) {
				server = GameObjects.ui.serverTextBox.getText();
			} else {
				server = NetworkHandler.DEFAULT_SERVER;
			}
			if (!server.isEmpty()) {
				GameObjects.ui.networkStatus.setText("Logging in...");
				if(NetworkHandler.login(server, GameObjects.ui.usernameTextBox.getText())){
					GameObjects.ui.setVisibleRecursive(false);
					GameObjects.ui.setVisible(true);
					GameObjects.ui.logoutButton.setVisible(true);
					GameObjects.players = new Player[50];
					IcePush.state = IcePush.PLAY;
				} else {
					GameObjects.ui.networkStatus.setText(NetworkHandler.message);
				}
			}
		}
	};

	public static Action onLogoutButtonClick = new Action() {
		public void doAction(UIComponent uiComp, int x, int y) {
			NetworkHandler.logOut();
			GameObjects.ui.setVisibleRecursive(false);
			GameObjects.ui.setVisible(true);
			GameObjects.ui.welcomeScreenContainer.setVisibleRecursive(true);
			state = WELCOME;
			GameObjects.ui.networkStatus.setText("Select a username.");
		}
	};

    // --- THIS CODE IS RUN ON THE EVENT DISPATCH THREAD --- //

    protected void processMouseEvent(MouseEvent e) {
        int id = e.getID();
        if (id == MouseEvent.MOUSE_CLICKED) {
            mouseClicked(e);
        } else if (id == MouseEvent.MOUSE_PRESSED) {
            mousePressed(e);
        } else if (id == MouseEvent.MOUSE_RELEASED) {
            mouseReleased(e);
        }
    }

    protected void processMouseMotionEvent(MouseEvent e) {
        int id = e.getID();
        if (id == MouseEvent.MOUSE_MOVED) {
            mouseMoved(e);
        } else if (id == MouseEvent.MOUSE_DRAGGED) {
            //mouseDragged(e);
        }
    }

    protected void processKeyEvent(KeyEvent e) {
        sendKeyEventInternal(e);
    }

    // --- --------------------------------------------- --- //

    private void sendKeyEventInternal(KeyEvent e) {
        int id = e.getID();
        if (id == KeyEvent.KEY_PRESSED) {
            keyPressed(e);
        } else if (id == KeyEvent.KEY_TYPED) {
            keyTyped(e);
        } else if (id == KeyEvent.KEY_RELEASED) {
            keyReleased(e);
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
		handleProjectiles(x, y);
    }

    //private void mouseDragged(MouseEvent e) {

    //}

    private void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if(GameObjects.ui != null)GameObjects.ui.handleAction(Actions.HOVER, x, y);
    }

    private void keyPressed(KeyEvent e) {
        if (IcePush.DEBUG)
            System.out.println("key pressed");

        try {
            keys[e.getKeyCode()] = true;
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (IcePush.DEBUG)
                System.out.println("Unrecognized key pressed!");
        }

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
			if(Renderer.chats_visible) {
				if (in_chat && !Renderer.curChat.trim().isEmpty()) {
					NetworkHandler.sendChatMessage(Renderer.curChat.trim());
					Renderer.curChat = "";
				}
				in_chat = !in_chat;
			}
		}
	}

    private void keyTyped(KeyEvent e) {
        if (in_chat && state == PLAY) {
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
        try {
            keys[e.getKeyCode()] = false;
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (IcePush.DEBUG)
                System.out.println("Unrecognized key released!");
        }
    }

	private static void checkKeys() {
		if(!in_chat) {
			if (keys[KeyEvent.VK_A]) {
				renderer.yaw += 3;
				renderer.updateCamera();
			}

			if (keys[KeyEvent.VK_D]) {
				renderer.yaw -= 3;
				renderer.updateCamera();
			}

			if (keys[KeyEvent.VK_W]) {
				renderer.pitch += 3;
				renderer.updateCamera();
			}
			if (keys[KeyEvent.VK_S]) {
				renderer.pitch -= 3;
				renderer.updateCamera();
			}

			if (keys[KeyEvent.VK_2])
				ClientRenderer.GRAPHICS_MODE = ClientRenderer.SOFTWARE_2D;
			if (keys[KeyEvent.VK_3])
				ClientRenderer.GRAPHICS_MODE = ClientRenderer.SOFTWARE_3D;
			if (keys[KeyEvent.VK_C] && !previous[KeyEvent.VK_C])
				Renderer.chats_visible = !Renderer.chats_visible;
			if (keys[KeyEvent.VK_X] && !previous[KeyEvent.VK_X])
				Renderer.deaths_visible = !Renderer.deaths_visible;
			if (keys[KeyEvent.VK_Q]) onLogoutButtonClick.doAction(GameObjects.ui.logoutButton, 0, 0);
		}
        if (keys[KeyEvent.VK_ESCAPE] && !isApplet)
            cleanup();

        // TODO make this not so bad
        if (keys[KeyEvent.VK_DOWN]) {
            angle = 0;
            if (keys[KeyEvent.VK_LEFT])
                angle -= 32;
            else if (keys[KeyEvent.VK_RIGHT])
                angle += 32;
        }
        if (keys[KeyEvent.VK_RIGHT]) {
            angle = 64;
            if (keys[KeyEvent.VK_UP])
                angle += 32;
            else if (keys[KeyEvent.VK_DOWN])
                angle -= 32;
        }
        if (keys[KeyEvent.VK_UP]) {
            angle = 128;
            if (keys[KeyEvent.VK_LEFT])
                angle += 32;
            else if (keys[KeyEvent.VK_RIGHT])
                angle -= 32;
        }
        if (keys[KeyEvent.VK_LEFT]) {
            angle = 192;
            if (keys[KeyEvent.VK_UP])
                angle -= 32;
            else if (keys[KeyEvent.VK_DOWN])
                angle += 32;
        }

        if (keys[KeyEvent.VK_PAGE_UP] && renderer.cameraZoom < 512) {
            renderer.cameraZoom += 8;
            renderer.updateCamera();
        }
        if (keys[KeyEvent.VK_PAGE_DOWN] && renderer.cameraZoom > 32) {
            renderer.cameraZoom -= 8;
            renderer.updateCamera();
        }
        System.arraycopy(keys, 0, previous, 0, 256);
    }


	private void handleProjectiles(int x, int y) {
		//System.out.println(e);
		Player you = GameObjects.players[NetworkHandler.id];
		if(you != null && !you.isDead) {
			x = x - you.sprite.x - you.sprite.width/2;
			y = you.sprite.y + you.sprite.height/2 - y;
			int a = (int)((128*Math.atan2(y, x))/Math.PI);
			if(a < 0) a += 256;
			NetworkHandler.sendProjectileRequest(a);
		}
	}

    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }
}
