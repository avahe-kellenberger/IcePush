package net.threesided.server;

import net.threesided.server.physics2d.Physics2D;
import net.threesided.shared.Constants;
import net.threesided.shared.InterthreadQueue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server implements Runnable {

    private static Map<String, String> defaults;

    static {
        Server.defaults = new HashMap<>();

        Server.defaults.put("bind-port", "2345");

        Server.defaults.put("irc-server", "irc.strictfp.com");
        Server.defaults.put("irc-channel", "#icepush");
        Server.defaults.put("irc-port", "6667");
        Server.defaults.put("irc-nick", "TestServer");

        Server.defaults.put("death-length", "0");
        Server.defaults.put("round-length", "90000");
    }

    private static int roundLength;
    private static int timeRemaining = roundLength;

    private static int deathLength;

    private static final String BAD_VERSION = "Your client is outdated.";
    private static final String USER_IN_USE = "Username is in use";
    private static final String TOO_MANY_PL = "There are too many players online.";

    private final Player[] incomplete = new Player[20];
    private Player[] players;

    private MapClass mapClass;

    private boolean run = true;
    private ServerSocket listener;
    private InterthreadQueue<Socket> incomingConnections;
    private ArrayList<String> chats;

    private int blockCount;

    public static void main(String[] args) {
        new Server(args);
    }

    private Server(String[] args) {
        this.players = new Player[30];
        final Map<String, String> settings = loadSettings("config");

        Server.roundLength = Integer.parseInt(settings.get("round-length"));
        Server.deathLength = Integer.parseInt(settings.get("death-length"));

        this.incomingConnections = new InterthreadQueue<>();

        final InternetRelayChat irc = new InternetRelayChat(settings.get("irc-server"), Integer.parseInt(settings.get("irc-port")),
                settings.get("irc-channel"), settings.get("irc-nick"));
        final Thread ircThread = new Thread(irc);
        ircThread.setDaemon(true);
        ircThread.start();

        final int port = Integer.parseInt(settings.get("bind-port"));
        try {
            this.listener = new ServerSocket(port);
        } catch (IOException ex) {
            System.out.println("Could not bind to port!");
            ex.printStackTrace();
        }
        System.out.println("Client listener started on port " + port);
        new Thread(this).start();

        final Physics2D physics = new Physics2D(this.players);
        this.mapClass = new MapClass();

        try {
            PacketMapper.load();
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }

        while (this.run) {
            final Socket socket = this.incomingConnections.pull();
            if (socket != null) {
                this.processIncomingConnection(socket);
            }
            this.updateIrc();
            physics.update();
            this.loginPlayers();
            this.updatePlayers();
            try {
                Thread.sleep(20);
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
            if (this.getNumPlayers() > 1) {
                Server.timeRemaining -= 20;
                if (Server.timeRemaining <= 0) {
                    resetDeaths();
                    Server.timeRemaining = Server.roundLength;
                }
            }
        }
    }

    private Map<String, String> loadSettings(final String fileName) {
        try {
            final Map<String, String> ret = new HashMap<>();
            final BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                final String[] parts = line.split(":");
                ret.put(parts[0], parts[1]);
            }
            for (String def : defaults.keySet()) {
                if (!ret.containsKey(def)) {
                    ret.put(def, defaults.get(def));
                }
            }
            return ret;
        } catch (final Exception ex) {
            return Server.defaults;
        }
    }

    public void run() {
        while (this.run)
            try {
                this.incomingConnections.push(this.listener.accept());
                Thread.sleep(30);
            } catch (final Exception ex) {
                System.out.println("Error accepting connections!");
                ex.printStackTrace();
                this.run = false;
            }
    }

    private void processIncomingConnection(final Socket socket) {
        System.out.println("Connection accepted: " + socket.toString());
        final String host = socket.getInetAddress().getHostName();
        if (host.endsWith("mia.bellsouth.net") || host.endsWith("anchorfree.com")) {
            this.blockCount++;
            if ((this.blockCount % 10) == 1)
                System.out.println("Blocked: " + this.blockCount + " times");
            try {
                socket.close();
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                socket.setTcpNoDelay(true);
                this.addLogin(new Player(new WebSocketBuffer(socket)));
            } catch (IOException ex) {
                System.out.println("Error processing connection!");
                ex.printStackTrace();
            }
        }
    }

    void addLogin(final Player p) {
        System.out.println("logging in");
        for (int i = 0; i < incomplete.length; i++) {
            if (incomplete[i] == null) {
                incomplete[i] = p;
                return;
            }
        }
        notifyFull(p);
    }

    void notifyFull(final Player p) {
        final WebSocketBuffer wsb = (WebSocketBuffer) p.pbuf;
        wsb.writeByte(Constants.FAILURE);
        wsb.writeString(TOO_MANY_PL);
        wsb.synch();
    }

    private void loginPlayers() {
        for (int i = 0; i < incomplete.length; i++) {
            loginPlayer(i);
        }
    }

    private void loginPlayer(int i) {
        final Player p = this.incomplete[i];
        if (p == null) {
            return;
        }

        try {
            final WebSocketBuffer wsb = (WebSocketBuffer) p.pbuf;

            if (!wsb.synch()) {
                this.incomplete[i] = null;
                return;
            }

            if (!p.readVer) {
                if (wsb.available() < 3) {
                    return;
                }
                final int op = wsb.openPacket();
                if (op == -1) {
                    return;
                }
                if (op != 0) {
                    incomplete[i] = null;
                    return;
                }
                final int version = wsb.readByte();
                p.readVer = true;
                if (version != Constants.VERSION) {
                    this.notifyBadVersion(i, wsb, BAD_VERSION);
                    return;
                }
                System.out.println("Op = " + op + " ver = " + version);
            }

            if (!p.readName) {
                final String name = wsb.readString();
                if (name == null) {
                    return;
                } else {
                    p.readName = true;
                    p.username = name;
                }

                for (final Player player : players) {
                    if (player != null) {
                        final String user = player.username;
                        if (user != null && user.equals(p.username) && player != p) {
                            wsb.beginPacket(Constants.FAILURE);
                            // name in use
                            wsb.writeString(USER_IN_USE);
                            wsb.endPacket();
                            wsb.synch();
                            incomplete[i] = null;
                            System.out.println("Username already in use: " + p.username);
                            return;
                        }
                    }
                }

                System.out.println("Player logged in: " + name);
            }

            int index = -1;
            for (int k = 0; k < this.players.length; k++) {
                if (this.players[k] == null) {
                    index = k;
                    break;
                }
            }

            if (index == -1) {
                this.notifyBadVersion(i, wsb, TOO_MANY_PL);
            }

            p.id = index;
            p.connected = true;
            System.out.println("p.id = " + p.id);
            p.type = index % 2;
            this.incomplete[i] = null;
            this.players[index] = p;

            // success
            wsb.beginPacket(Constants.SUCCESS_LOG);
            wsb.writeByte(p.id);
            wsb.endPacket();
            wsb.synch();
            for (final Player plr : this.players) {
                if (plr != null && plr != p) {
                    // Tell p about all players already logged in
                    p.notifyLogin(plr);
                    // Tell all already logged in players about p
                    plr.notifyLogin(p);
                    p.handleMove(plr);
                    plr.handleMove(p);
                }
            }
            p.notifyLogin(p);
            p.initPosition(this.players, this.mapClass.currentPath);
            System.out.println("Player logged in: " + p.username + ", id: " + p.id);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    private void notifyBadVersion(int i, WebSocketBuffer wsb, String badVersion) {
        wsb.beginPacket(Constants.FAILURE);
        wsb.writeString(badVersion);
        wsb.endPacket();
        wsb.synch();
        this.incomplete[i] = null;
    }

    private void updateIrc() {
        InternetRelayChat.processInput();
        this.chats = new ArrayList<>();
        String msg;
        while ((msg = InternetRelayChat.msgs.pull()) != null) {
            this.chats.add(msg);
        }

        String username;
        while ((username = InternetRelayChat.kicks.pull()) != null) {
            for (final Player p : this.players) {
                if (p != null) {
                    if (p.username.toLowerCase().equals(username)) {
                        this.logoutPlayer(p);
                        InternetRelayChat.sendMessage("Player " + username + " has been kicked.");
                    }
                }
            }
        }
    }

    private void updatePlayers() {
        for (final Player p : this.players) {
            if (p == null || !p.connected) {
                continue;
            }

            if (!p.processIncomingPackets() || p.logOut) {
                this.logoutPlayer(p);
            } else {
                p.writePendingChats(this.chats);
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
                        p.initPosition(this.players, this.mapClass.currentPath);
                    }
                    if (p.timeDead % 1000 == 0) {
                        p.updateDeathTime(p.timeDead / 1000);
                    }
                }

                if (!this.mapClass.currentPath.contains(p.position.getX(), p.position.getY()) && !p.isDead) {
                    System.out.println("PLAYER " + p.username + " IS OUT OF RANGE!");
                    if (getNumPlayers() > 1) {
                        p.deaths++;
                        p.deaths %= 128;
                    }
                    p.isDead = true;
                    p.timeDead = deathLength;
                    p.updateDeathTime(p.timeDead / 1000);

                    // plr cycles through every player; p is the player who just died
                    for (final Player plr : this.players) {
                        if (plr != null) {
                            plr.playerDied(p);
                        }
                    }

                }

                if (p.hasMoved() && !p.isDead) {
                    for (final Player plr : this.players)
                        if (plr != null) {
                            plr.handleMove(p);
                        }
                }
            }

            if (this.getNumPlayers() > 1 && Server.timeRemaining % 1000 == 0) {
                p.updateRoundTime(Server.timeRemaining / 1000);
            }
        }
    }

    private void logoutPlayer(Player p) {
        try {
            for (final Player plr : this.players) {
                if (plr == null || plr == p)
                    continue;

                plr.loggedOut(p);
            }

            p.connected = false;
            this.players[p.id] = null;
            System.out.println("Logged out: " + p.username);

            if (this.getNumPlayers() < 2) {
                Server.timeRemaining = -1;
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getNumPlayers() {
        int count = 0;
        for (final Player p : this.players)
            if (p != null) {
                count++;
            }
        return count;
    }

    private void resetDeaths() {
        for (final Player p : this.players) {
            if (p != null) {
                p.deaths = 0;
            }
        }
    }

}
