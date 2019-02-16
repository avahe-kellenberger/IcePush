package net.threesided.server;

import net.threesided.server.net.WebSocketBuffer;
import net.threesided.server.net.event.OPCode;
import net.threesided.server.net.event.ServerNetworkEvent;
import net.threesided.server.net.event.events.client.LoginEvent;
import net.threesided.server.net.event.events.server.FailureEvent;
import net.threesided.server.net.event.events.server.LoginSuccessEvent;
import net.threesided.server.net.event.events.server.NewPlayerEvent;
import net.threesided.server.physics2d.Updatable;
import net.threesided.shared.*;
import net.threesided.util.LoopedThreadedTask;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.Supplier;

public class Server implements Updatable {

    private final Game game;
    private final Rectangle2D gameArea;
    private final HashMap<Player, PacketBuffer> clients;
    private final InterthreadQueue<PacketBuffer> incomingConnections;
    private final InterthreadQueue<ServerNetworkEvent> serverEventQueue;
    private final LoopedThreadedTask gameLoopTask, clientConnectionAcceptorTask;

    private ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {
        final Server server = new Server();
        server.start(2345);
    }

    /**
     *
     */
    public Server() {
        this.game = new Game();
        this.gameArea = new Rectangle(28, 30, 746, 424);
        this.clients = new HashMap<>();
        this.incomingConnections = new InterthreadQueue<>();
        this.serverEventQueue = new InterthreadQueue<>();

        // Condition for the server to continue running.
        final Supplier<Boolean> serverRunCondition = () -> this.serverSocket != null && !this.serverSocket.isClosed();

        // Accept all incoming connections.
        this.clientConnectionAcceptorTask = new LoopedThreadedTask((elapsedTime) -> this.acceptConnections(), serverRunCondition, 0);

        // 60 FPS game loop update.
        final int FPS = 60;
        final double frameDurationSeconds = 1.0 / FPS;
        this.gameLoopTask = new LoopedThreadedTask(this::update, serverRunCondition, frameDurationSeconds);
    }

    /**
     * Starts the server.
     * @param port The port the server will listen on.
     * @throws IOException If an I/O error occurs when opening the server's socket.
     */
    public void start(final int port) throws IOException {
        this.serverSocket = new ServerSocket(port);

        // Start listening for incoming client connections.
        this.clientConnectionAcceptorTask.start();

        // Starts the game update loop task.
        this.gameLoopTask.start();
    }

    /**
     * Signals the server to stop.
     * @throws IOException If the `ServerSocket` throws an exception when closed.
     */
    public void stop() throws IOException {
        this.gameLoopTask.stop();
        this.clientConnectionAcceptorTask.stop();
        this.serverSocket.close();
    }

    /**
     * Accepts incoming connections.
     */
    private void acceptConnections() {
        try {
            final Socket socket = this.serverSocket.accept();
            socket.setTcpNoDelay(true);
            this.incomingConnections.push(new WebSocketBuffer(socket));
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Processes incoming client connections.
     */
    private void processConnectingPlayers() {
        PacketBuffer clientSocketBuffer;
        while ((clientSocketBuffer = this.incomingConnections.pull()) != null) {
            // TODO: Can't remove clients from list if this method returns false.
            this.processNewPlayerConnection(clientSocketBuffer);
        }
    }

    /**
     *
     * @param packetBuffer The buffer associated with the newly connecting player.
     * @return
     */
    private boolean processNewPlayerConnection(final PacketBuffer packetBuffer) {
        if (!this.isNewConnectionValid(packetBuffer)) {
            return false;
        }

        final Player player = this.tryCreateNewPlayer(packetBuffer);
        if (player == null) {
            return false;
        }
        // Successfully created a new player - add it to the server.
        this.addNewPlayer(player, packetBuffer);

        // Enqueue login success event to the new player.
        this.enqueueEvent(new LoginSuccessEvent(packetBuffer, player.getID()));

        // Enqueue player logged in event to all players.
        this.enqueueEvent(new NewPlayerEvent(this.clients.values(), player));

        // Set player's initial position.
        final Vector2D randomLocation = this.getRandomGameAreaLocation();
        player.setLocation(randomLocation);

        // TODO: Notify new player of the current round or victory lap with time remaining.

        return true;
    }

    /**
     * @return A random point within the game's play area.
     */
    private Vector2D getRandomGameAreaLocation() {
        final double randomX = MathUtils.random(this.gameArea.getMinX(), this.gameArea.getMaxX());
        final double randomY = MathUtils.random(this.gameArea.getMinY(), this.gameArea.getMaxY());
        return new Vector2D(randomX, randomY);
    }

    /**
     * Attempts to create a player from the incoming connection.
     *
     * This method will return null if either:
     * A. The data sent by the player to login is incomplete or failed (see Server#readPlayerLoginPacket).
     * B. The current world is full.
     *
     * @param clientSocketBuffer The socket to the new player.
     * @return A player created by the data stored in the Buffer via the client's connection.
     */
    private Player tryCreateNewPlayer(final PacketBuffer clientSocketBuffer) {
        final LoginEvent loginEvent = this.readPlayerLoginPacket(clientSocketBuffer);
        if (!this.validateLogin(clientSocketBuffer, loginEvent)) {
            return null;
        }

        if (this.isGameFull()) {
            final FailureEvent event = new FailureEvent(clientSocketBuffer, "There are too many players online.");
            this.enqueueEvent(event);
            return null;
        }
        return this.createNewPlayer(loginEvent.playerName);
    }

    /**
     * Attempts to read data from the buffer to create a `LoginEvent`.
     * This data may have not been sent yet, and will result in returning `null`.
     *
     * @param clientBuffer The buffer of the connected client.
     * @return A LoginEvent constructed from data sent by the client, or null.
     */
    private LoginEvent readPlayerLoginPacket(final PacketBuffer clientBuffer) {
        final int opcode = clientBuffer.openPacket();
        if (OPCode.getByValue(opcode) != OPCode.LOGIN) {
            return null;
        }
        return new LoginEvent(clientBuffer);
    }

    /**
     * Checks if the LoginEvent contains data appropriate for the player to join the current game.
     * If there are any issues with the login, an appropriate event will be sent to the client.
     *
     * @param clientBuffer The buffer of the connected client.
     * @param loginEvent The LoginEvent to validate.
     * @return If the login was successful and valid.
     */
    private boolean validateLogin(final PacketBuffer clientBuffer, final LoginEvent loginEvent) {
        if (loginEvent == null) {
            return false;
        }

        if (loginEvent.clientVersion != Constants.VERSION) {
            final FailureEvent event = new FailureEvent(clientBuffer, "Your client is outdated.");
            this.enqueueEvent(event);
            return false;
        }

        if (!this.isUsernameAvailable(loginEvent.playerName)) {
            final FailureEvent event = new FailureEvent(clientBuffer, "Username is in use.");
            this.enqueueEvent(event);
            return false;
        }

        // Successful login.
        return true;
    }

    /**
     * Checks if the newly connected buffer is in a valid state
     * and responding correctly to be used as a client for the game.
     *
     * @param packetBuffer The buffer of which to be checked.
     * @return If the new connection is in a valid state for the game.
     */
    private boolean isNewConnectionValid(final PacketBuffer packetBuffer) {
        return packetBuffer.sync();
    }

    /**
     * Creates a new player with the given username.
     * This function does not validate connectivity or username use status.
     *
     * @param username The username of the player to add.
     * @return A newly created player with the given username.
     */
    private Player createNewPlayer(final String username) {
        final byte playerID = this.getNextAvailablePlayerID();
        final Player.Type type = this.getNextAvailablePlayerType();
        return new Player(playerID, type, username, Player.DEFAULT_LIVES);
    }

    /**
     * Adds a new player to the game and server.
     * @param player The player to add.
     */
    private void addNewPlayer(final Player player, final PacketBuffer clientBuffer) {
        this.game.add(player);
        this.clients.put(player, clientBuffer);
    }

    /**
     * @return The next available player type, based on the current players in the game.
     */
    private Player.Type getNextAvailablePlayerType() {
        final int playerCount = this.clients.size();
        return Player.Type.getByID((byte) (playerCount % Player.Type.values().length));
    }

    /**
     * @return The next available player ID, or -1 if the game is full.
     */
    private byte getNextAvailablePlayerID() {
        // TODO: Implement.
        return 1;
    }

    /**
     * @return If the game is full, and cannot accept more players.
     */
    private boolean isGameFull() {
        // TODO: Implement.
        return false;
    }

    /**
     * @param username The username to check for.
     * @return If the given username is not currently in use by another player.
     */
    private boolean isUsernameAvailable(final String username) {
        for (final Player player : this.clients.keySet()) {
            if (player.getUsername().equalsIgnoreCase(username)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Enqueues a `ServerNetworkEvent` to be sent on the next game update.
     * @param event The event to send.
     */
    private void enqueueEvent(final ServerNetworkEvent event) {
        this.serverEventQueue.push(event);
    }

    @Override
    public void update(final double elapsed) {
        this.game.update(elapsed);
        this.processGameStateChanges();
        this.sendUpdatesToClients();
        this.processConnectingPlayers();
    }

    /**
     * Prepares `ServerNetworkEvents` based on changes to the game's state since the last update.
     */
    private void processGameStateChanges() {
        // Use Entity#hasMoved for locations.
        this.clients.keySet().forEach(player -> {
            if (player.hasMoved()) {
                // TODO:
                // this.serverEventQueue.push(new MoveEvent(this.clients.values(), player));
            }
        });
    }

    /**
     * Sends the updated game state to the clients (players).
     */
    private void sendUpdatesToClients() {
        ServerNetworkEvent event;
        while ((event = this.serverEventQueue.pull()) != null) {
            System.out.println(event.getOPCode());
            event.writeToRecipients();
        }
    }

}
