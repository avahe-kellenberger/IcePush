package net.threesided.server;

import static net.threesided.shared.Constants.FAILURE;
import static net.threesided.shared.Constants.SUCCESS_LOG;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;

import net.threesided.server.physics2d.Physics2D;

import net.threesided.shared.InterthreadQueue;
import net.threesided.shared.Constants;

public class Server implements Runnable {
    //private boolean DEBUG = false;
    private Player[] players;
    //private Socket worldserver;

    //private UpdateServer updates;
    private MapClass mapClass;

    private boolean run = true;
    private ServerSocket listener;
    private InterthreadQueue<Socket> incomingConnections;
    private ArrayList<String> chats;

    private int blockCount;

    private static int roundLength;
    private static int timeRemaining = roundLength;

    public static int deathLength;

    private static final String BAD_VERSION = "Your client is outdated.";
    private static final String USER_IN_USE = "Username is in use";
    private static final String TOO_MANY_PL = "There are too many players online.";

    public static void main(String[] args) {
        new Server(args);
    }

    private Server(String[] args) {
        //if (args.length > 0 && args[0].equalsIgnoreCase("-debug")) DEBUG = true;
        players = new Player[30];
        Map<String, String> settings = loadSettings("config");

        roundLength = Integer.parseInt(settings.get("round-length"));
        deathLength = Integer.parseInt(settings.get("death-length"));

        //if(Boolean.parseBoolean(settings.get("show-in-list")))
        //	worldserver = connectToWorldServer(settings
        //			.get("worldserver-addr"), Constants.WORLDPORT);

        incomingConnections = new InterthreadQueue<Socket>();

        InternetRelayChat irc = new InternetRelayChat(settings.get("irc-server"), Integer.parseInt(settings.get("irc-port")),
                settings.get("irc-channel"), settings.get("irc-nick"));
        Thread t = new Thread(irc);
        t.setDaemon(true);
        t.start();

        int port = Integer.parseInt(settings.get("bind-port"));
        try {
            listener = new ServerSocket(port);
        } catch (IOException ioe) {
            System.out.println("Could not bind to port!");
            ioe.printStackTrace();
        }
        System.out.println("Client listener started on port " + port);
        new Thread(this).start();

        Physics2D physics = new Physics2D(players);
        //updates = new UpdateServer(new File(settings.get("update-path")));
        //updates.start();
        mapClass = new MapClass();

        try {
            PacketMapper.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        while (run) {
            Socket s = incomingConnections.pull();
            if (s != null) processIncomingConnection(s);
            updateIrc();
            physics.update();
            updatePlayers();
            try {
                Thread.sleep(20);
            } catch (Exception e) {
                 e.printStackTrace();
            }
            if (getNumPlayers() > 1) {
                timeRemaining -= 20;
                if (timeRemaining <= 0) {
                    resetDeaths();
                    timeRemaining = roundLength;
                }
            }
        }
    }

    private Map<String, String> loadSettings(String fn) {
        try {
            Map<String, String> ret = new HashMap<String, String>();
            BufferedReader br = new BufferedReader(new FileReader(fn));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                ret.put(parts[0], parts[1]);
            }
            for (String def : defaults.keySet()) {
                if (!ret.containsKey(def))
                    ret.put(def, defaults.get(def));
            }
            return ret;
        } catch (Exception e) {
            return defaults;
        }
    }

    /*private Socket connectToWorldServer(String server, int port) {
        System.out.print("Connecting to worldserver: " + server + ":" + port + "...");
        try {
            Socket sock = new Socket(server, port);
            System.out.print("connected...");
            OutputStream out = sock.getOutputStream();
            out.write(Constants.NEW_SERVER);
            System.out.print("Constants.NEW_SERVER sent...");
            String host = settings.get("host");
            System.out.print("host (" + host + ") sent...");
            out.write(host.length());
            out.write(host.getBytes());
            out.write(getNumPlayers());
            out.flush();
            System.out.println("stream flushed.");
            return sock;
        } catch (Exception e) {
            return null;
        }
    }*/

    public void run() {
        while (run)
            try {
                incomingConnections.push(listener.accept());
                Thread.sleep(30);
            } catch (Exception ioe) {
                System.out.println("Error accepting connections!");
                ioe.printStackTrace();
                run = false;
                //updates.run = false;
            }
    }

    private void processIncomingConnection(Socket s) {
        System.out.println("Connection accepted: " + s.toString());
        String host = s.getInetAddress().getHostName();
        if (host.endsWith("mia.bellsouth.net") || host.endsWith("anchorfree.com")) {
            blockCount++;
            if ((blockCount % 10) == 1)
                System.out.println("Blocked: " + blockCount + " times");
            try {
                s.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                s.setTcpNoDelay(true);
                int type = s.getInputStream().read();
                if (type == 0) { // connecting client
                    loginPlayer(s);
                } else if (type == 2) {
                    s.getOutputStream().write(getNumPlayers());
                } else if (type == 3) {
                    //updates.incomingConnections.push(s);	// THIS SERVER IS HELD TOGETHER WITH DUCT TAPE
                }
            } catch (IOException ioe) {
                System.out.println("Error processing connection!");
                ioe.printStackTrace();
            }
        }
    }

    private void loginPlayer(Socket s) {
        try {
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();
            int version = in.read();
            if (version != Constants.VERSION) {
                out.write(FAILURE); // bad version
                out.write(BAD_VERSION.length() & 0xFF);
                out.write(BAD_VERSION.getBytes());
                out.flush();
                return;
            }
            int len = in.read();
            byte[] strb = new byte[len];
            int read = in.read(strb);
            if(len != read)
                return;
            Player p = new Player(s);
            p.username = new String(strb);

            for (Player player : players) {
                if (player != null) {
                    String user = player.username;
                    if (user != null && user.equals(p.username)) {
                        out.write(FAILURE); // name in use
                        out.write(USER_IN_USE.length() & 0xFF);
                        out.write(USER_IN_USE.getBytes());
                        out.flush();
                        return;
                    }
                }
            }

            int index = -1;
            for (int k = 0; k < players.length; k++)
                if (players[k] == null) {
                    index = k;
                    break;
                }

            if (index == -1) {
                out.write(FAILURE);
                out.write(TOO_MANY_PL.length() & 0xFF);
                out.write(TOO_MANY_PL.getBytes());
                out.flush();
                return;
            }

            p.id = index;
            p.type = index % 2;

            out.write(SUCCESS_LOG); // success
            out.write(p.id);
            out.flush();

            p.connected = true;
            players[p.id] = p;
            for (Player plr : players)
                if (plr != null) {
                    p.notifyLogin(plr);        // Tell p about all players already logged in
                    plr.notifyLogin(p);        // Tell all already logged in players about p
                    p.handleMove(plr);
                    plr.handleMove(p);
                }
            p.initPosition(players, mapClass.currentPath);
            System.out.println("Player logged in: " + p.username + ", id: " + p.id);
            syncNumPlayers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncNumPlayers() {
        /*	if((worldserver == null) || !worldserver.isConnected())
              return;
          try {
              OutputStream out = worldserver.getOutputStream();
              out.write(Constants.NUM_PLAYERS_NOTIFY);
              out.write(getNumPlayers());
              out.flush();
          } catch(Exception e) {
              e.printStackTrace();
          }*/
    }

    private void updateIrc() {
        InternetRelayChat.processInput();
        chats = new ArrayList<String>();
        String msg;
        while ((msg = InternetRelayChat.msgs.pull()) != null) chats.add(msg);
        String kick;
        while ((kick = InternetRelayChat.kicks.pull()) != null) {
            for (Player p : players) {
                if (p != null) {
                    if (p.username.toLowerCase().equals(kick)) {
                        logoutPlayer(p);
                        InternetRelayChat.sendMessage("Player " + kick + " has been kicked.");
                    }
                }
            }
        }
    }

    private void updatePlayers() {
        for (Player p : players) {
            if (p == null || !p.connected)
                continue;
            if (!p.processIncomingPackets() || p.logOut) {
                logoutPlayer(p);
            } else {
                p.writePendingChats(chats);
                if (p.chatMessage != null) {
                    InternetRelayChat.sendMessage(p.chatMessage);
                    InternetRelayChat.msgs.push(p.chatMessage);
                    p.chatMessage = null;
                }

                if (p.isDead) {
                    if (p.timeDead >= 0) {
                        p.timeDead -= 20;
                    } else {
                        p.isDead = false;
                        p.timeDead = 0;
                        p.initPosition(players, mapClass.currentPath);
                    }
                    if (p.timeDead % 1000 == 0) p.updateDeathTime(p.timeDead / 1000);
                }

                if (!mapClass.currentPath.contains(p.position.getX(), p.position.getY()) && !p.isDead) {
                    System.out.println("PLAYER " + p.username + " IS OUT OF RANGE!");
                    if (getNumPlayers() > 1) {
                        p.deaths++;
                        p.deaths %= 128;
                    }
                    p.isDead = true;
                    p.timeDead = deathLength;
                    p.updateDeathTime(p.timeDead / 1000);

                    for (Player plr : players)
                        if (plr != null)
                            plr.playerDied(p);    // plr cycles through every player; p is the player who just died

                }

                if (p.hasMoved() && !p.isDead) {
                    for (Player plr : players) if (plr != null) plr.handleMove(p);
                }
            }

            if (getNumPlayers() > 1 && timeRemaining % 1000 == 0) p.updateRoundTime(timeRemaining / 1000);
        }
    }

    private void logoutPlayer(Player p) {
        try {
            for (Player plr : players) {
                if (plr == null || plr == p)
                    continue;

                plr.loggedOut(p);
            }

            p.connected = false;
            players[p.id] = null;
            System.out.println("Logged out: " + p.id);

            syncNumPlayers();
            if (getNumPlayers() < 2) {
                timeRemaining = -1;
                //System.out.println(getNumPlayers());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getNumPlayers() {
        int count = 0;
        for (Player p : players)
            if (p != null)
                count++;
        return count;
    }

    private void resetDeaths() {
        for (Player p : players)
            if (p != null)
                p.deaths = 0;
    }

    private static Map<String, String> defaults;

    static {
        defaults = new HashMap<String, String>();

        defaults.put("bind-port", "2345");

        defaults.put("irc-server", "irc.strictfp.com");
        defaults.put("irc-channel", "#icepush");
        defaults.put("irc-port", "6667");
        defaults.put("irc-nick", "TestServer");

        defaults.put("death-length", "0");
        defaults.put("round-length", "90000");

        /* Worldserver and updateserver temporarily disabled for the time being */
        //defaults.put("worldserver-addr", "99.198.122.53");
        //defaults.put("show-in-list", "true");
        //defaults.put("update-path", "/home/icepush/data");
    }
}
