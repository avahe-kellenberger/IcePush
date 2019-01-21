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

        Server.defaults.put("irc-server", "irc.rizon.net");
        Server.defaults.put("irc-channel", "#icepush");
        Server.defaults.put("irc-port", "6697");
        Server.defaults.put("irc-nick", "TestServer");

        Server.defaults.put("death-length", "0");
        Server.defaults.put("round-length", "25000");
    }

    private static final String BAD_VERSION = "Your client is outdated.";
    private static final String USER_IN_USE = "Username is in use";
    private static final String TOO_MANY_PL = "There are too many players online.";
    private static final int DEFAULT_LIVES = 5;

    private static int roundLength;
    private static int timeRemaining = -1000;

    private static int deathLength;

    private final Player[] incomplete = new Player[20];
    private Player[] players;
    private MapClass mapClass;

    private boolean run = true;
    private boolean roundStarted = false;
    private ServerSocket listener;
    private InterthreadQueue<Socket> incomingConnections;
    private ArrayList<String> chats;

    private int blockCount;
    private String victoryString;

    public static void main(final String[] args) {
        boolean runLocal = false;
        if (args != null && args.length >= 1) {
            runLocal = Boolean.valueOf(args[0]);
        }
        new Server(runLocal);
    }

    /**
     * @param runLocal If the server should be ran locally.
     *                 If true, the server will not make external connections (e.g. to the IRC server).
     */
    private Server(final boolean runLocal) {
        this.players = new Player[30];
        final Map<String, String> settings = loadSettings("server.config");

        Server.roundLength = Integer.parseInt(settings.get("round-length"));
        Server.deathLength = Integer.parseInt(settings.get("death-length"));

        this.incomingConnections = new InterthreadQueue<>();

        if (!runLocal) {
            final InternetRelayChat irc = new InternetRelayChat(
                    settings.get("irc-server"),
                    Integer.parseInt(settings.get("irc-port")),
                    settings.get("irc-channel"),
                    settings.get("irc-nick"));

            final Thread thread = new Thread(irc);
            thread.setDaemon(true);
            thread.start();
        }

        final int port = Integer.parseInt(settings.get("bind-port"));
        try {
            this.listener = new ServerSocket(port);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
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
            try {
                Thread.sleep(20);
            } catch (final Exception ex) {
                ex.printStackTrace();
            }

            this.roundStarted = getNumPlayers() > 1;

            if (this.roundStarted) {
                if (Server.timeRemaining % 1000 == 0) {
                    updateRoundTime();
                }

                if (this.getLivingPlayers() == 1) {
                    Server.timeRemaining = 20;
                }

                Server.timeRemaining -= 20;
                if (Server.timeRemaining <= 0) {
                    if (Server.timeRemaining == 0) {
                        final String msg = this.victoryString;
                        if (InternetRelayChat.sendWinner) {
                            InternetRelayChat.sendMessage(msg);
                        }
                        this.updateWinners(this.getWinners());
                    }
                    this.resetDeaths();
                    Server.timeRemaining = Server.roundLength;
                }
            }
            this.updatePlayers();
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
            for (final String def : Server.defaults.keySet()) {
                if (!ret.containsKey(def)) {
                    ret.put(def, Server.defaults.get(def));
                }
            }
            return ret;
        } catch (final Exception ex) {
            return Server.defaults;
        }
    }

    public void run() {
        while (this.run) {
            try {
                this.incomingConnections.push(this.listener.accept());
                Thread.sleep(30);
            } catch (final Exception ex) {
                ex.printStackTrace();
                this.run = false;
            }
        }
    }

    private void processIncomingConnection(final Socket socket) {
        final String host = socket.getInetAddress().getHostName();
        if (host.endsWith("mia.bellsouth.net") || host.endsWith("anchorfree.com")) {
            this.blockCount++;
            if ((this.blockCount % 10) == 1)
                try {
                    socket.close();
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
        } else {
            try {
                socket.setTcpNoDelay(true);
                this.addLogin(new Player(new WebSocketBuffer(socket)));
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addLogin(final Player p) {
        for (int i = 0; i < this.incomplete.length; i++) {
            if (this.incomplete[i] == null) {
                this.incomplete[i] = p;
                return;
            }
        }
        this.notifyFull(p);
    }

    private void notifyFull(Player p) {
        final WebSocketBuffer wsb = (WebSocketBuffer) p.pbuf;
        wsb.writeByte(Constants.FAILURE);
        wsb.writeString(Server.TOO_MANY_PL);
        wsb.synch();
    }

    private void loginPlayers() {
        for (int i = 0; i < this.incomplete.length; i++) {
            this.loginPlayer(i);
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
                    this.incomplete[i] = null;
                    return;
                }
                final int version = wsb.readByte();
                p.readVer = true;
                if (version != Constants.VERSION) {
                    this.sendBadVersionEvent(i, wsb, Server.BAD_VERSION);
                    return;
                }
            }

            if (!p.readName) {
                final String name = wsb.readString();
                if (name == null) {
                    return;
                } else {
                    p.readName = true;
                    p.username = name;
                }

                for (final Player player : this.players) {
                    if (player != null) {
                        final String user = player.username;
                        if (user != null && user.equals(p.username) && player != p) {
                            wsb.beginPacket(Constants.FAILURE);
                            // name in use
                            wsb.writeString(Server.USER_IN_USE);
                            wsb.endPacket();
                            wsb.synch();
                            this.incomplete[i] = null;
                            return;
                        }
                    }
                }
            }

            int index = -1;
            for (int k = 0; k < this.players.length; k++) {
                if (this.players[k] == null) {
                    index = k;
                    break;
                }
            }

            if (index == -1) {
                this.sendBadVersionEvent(i, wsb, Server.TOO_MANY_PL);
            }

            p.id = index;
            p.connected = true;
            p.type = index % 2;
            p.lives = DEFAULT_LIVES;
            this.incomplete[i] = null;
            this.players[index] = p;

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
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendBadVersionEvent(final int i, final WebSocketBuffer wsb, final String badVersion) {
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
            chats.add(msg);
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
                        p.timeDead = 0;
                    }
                }

                if (!this.mapClass.currentPath.contains(p.position.getX(), p.position.getY()) && !p.isDead) {
                    if (this.roundStarted) {
                        p.lives--;
                    }
                    if (p.lives == 0) {
                        p.isDead = true;
                    } else {
                        p.initPosition(this.players, this.mapClass.currentPath);
                    }
                    p.timeDead = deathLength;

                    for (final Player plr : this.players) {
                        if (plr != null) {
                            // plr cycles through every player; p is the player who just died
                            plr.updateLives(p);
                        }
                    }
                }

                if (p.hasMoved() && !p.isDead) {
                    for (final Player plr : this.players) {
                        if (plr != null) {
                            plr.handleMove(p);
                        }
                    }
                }
            }
        }
    }

    private void updateRoundTime() {
        for (final Player p : this.players) {
            if (p != null) {
                p.updateRoundTime(Server.timeRemaining / 1000);
            }
        }
    }

    private void logoutPlayer(final Player p) {
        try {
            for (final Player plr : this.players) {
                if (plr != null && plr != p) {
                    plr.loggedOut(p);
                }
            }

            p.connected = false;
            this.players[p.id] = null;

            if (this.getNumPlayers() < 2) {
                this.roundStarted = false;
                Server.timeRemaining = -1000;
                resetDeaths();
                updateRoundTime();
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

    private int getLivingPlayers() {
        int count = 0;
        for (final Player plr : this.players) {
            if (plr != null) {
                if (!plr.isDead) {
                    count++;
                }
            }
        }
        return count;
    }

    private void updateWinners(byte[] b) {
        for (final Player p : this.players) {
            if (p != null) {
                p.updateWinners(b);
            }
        }
    }

    private byte[] getWinners() {
        Player first = null, second = null;
        int fl = -1, sl = -1, tl = -1;
        for (final Player p : this.players)
            if (p != null) {
                if (p.lives > fl) {
                    tl = sl;
                    sl = fl;
                    second = first;
                    fl = p.lives;
                    first = p;
                } else if (p.lives > sl) {
                    tl = sl;
                    sl = p.lives;
                    second = p;
                } else if (p.lives > tl) {
                    tl = p.lives;
                }
            }

        if (fl == sl && sl == tl) {
            this.victoryString = "All of you are losers";
            return new byte[0];
        }
        if (fl > sl) {
            this.victoryString = "PLAYER " + first.username + " HAS WON AND IS NOW THE WINNER !";
            return new byte[]{(byte) first.id};
        }
        if (fl == sl) {
            this.victoryString = "PLAYERS " + first.username + " AND " + second.username + " HAVE WON AND ARE NOW THE WINNERS ! !";
            return new byte[]{(byte) first.id, (byte) second.id};
        }
        this.victoryString = "hello";
        return new byte[0];
    }

    private void resetDeaths() {
        for (final Player player : this.players) {
            if (player != null) {
                if (player.isDead) {
                    player.initPosition(this.players, this.mapClass.currentPath);
                }
                player.lives = DEFAULT_LIVES;
                for (final Player plr : this.players) {
                    if (plr != null) {
                        // Tell p about all players already logged in
                        if (player.isDead) {
                            plr.notifyLogin(player);
                            plr.handleMove(player);
                        }
                        plr.updateLives(player);
                    }
                }
                player.isDead = false;
            }
        }
    }
}
