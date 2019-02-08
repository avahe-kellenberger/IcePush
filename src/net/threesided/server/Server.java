package net.threesided.server;

import net.threesided.server.net.WebSocketBuffer;
import net.threesided.server.net.event.OPCode;
import net.threesided.server.net.event.ServerNetworkEvent;
import net.threesided.server.net.event.events.client.LoginEvent;
import net.threesided.server.net.event.events.server.FailureEvent;
import net.threesided.server.physics2d.Updatable;
import net.threesided.shared.Constants;
import net.threesided.shared.InterthreadQueue;
import net.threesided.util.LoopedThreadedTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public class Server implements Updatable {

    private final Game game;
    private final InterthreadQueue<Socket> incomingConnections;
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
            final WebSocketBuffer clientSocketBuffer = this.constructClientConnection(socket);

            // Terminate the connection if it was not successful.
            if (clientSocketBuffer == null || !this.tryAddNewPlayer(clientSocketBuffer)) {
                socket.close();
            }
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Processes the newly connected socket and checks for validity.
     * @param socket The client socket to process after a connection is established.
     * @return A WebSocketBuffer connected to the client, or null if the connection failed to validate.
     */
    private WebSocketBuffer constructClientConnection(final Socket socket) {
        try {
            socket.setTcpNoDelay(true);
            this.incomingConnections.push(socket);
            final WebSocketBuffer clientSocketBuffer = new WebSocketBuffer(socket);
            if (!this.isNewConnectionValid(clientSocketBuffer)) {
                return null;
            }
            return clientSocketBuffer;
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Attempts to create and add a player from the incoming connection.
     * @param clientSocketBuffer The socket to the new player.
     * @return If the player was successfully added to the game.
     */
    private boolean tryAddNewPlayer(final WebSocketBuffer clientSocketBuffer) {
        final LoginEvent loginEvent = this.waitForPlayerLogin(clientSocketBuffer);
        if (!this.validateLogin(clientSocketBuffer, loginEvent)) {
            return false;
        }

        if (this.isGameFull()) {
            final FailureEvent event = new FailureEvent(clientSocketBuffer, "There are too many players online.");
            this.enqueueEvent(event);
            return false;
        }

        final Player player = this.createNewPlayer(loginEvent.playerName);
        this.addNewPlayer(player, clientSocketBuffer);

        // TODO: Enqueue login success event to player
        // TODO: Enqueue player logged in event to players

        // TODO: Set player's position, which should automatically trigger move events

        // TODO: Notify new player of the current round or victory lap with time remaining.

        return true;
    }

    /**
     * Waits for the client to send data for a LoginEvent.
     * @param clientSocketBuffer The WebSocketBuffer of the connected client.
     * @return A LoginEvent constructed from data sent by the client.
     */
    private LoginEvent waitForPlayerLogin(final WebSocketBuffer clientSocketBuffer) {
        final int opcode = clientSocketBuffer.openPacket();
        if (OPCode.getByValue(opcode) != OPCode.LOGIN) {
            return null;
        }
        return new LoginEvent(clientSocketBuffer);
    }

    /**
     * Checks if the LoginEvent contains data appropriate for the player to join the current game.
     * If there are any issues with the login, an appropriate event will be sent to the client.
     *
     * @param clientSocketBuffer The WebSocketBuffer of the connected client.
     * @param loginEvent The LoginEvent to validate.
     * @return If the login was successful and valid.
     */
    private boolean validateLogin(final WebSocketBuffer clientSocketBuffer, final LoginEvent loginEvent) {
        if (loginEvent == null) {
            return false;
        }

        if (loginEvent.clientVersion != Constants.VERSION) {
            final FailureEvent event = new FailureEvent(clientSocketBuffer, "Your client is outdated.");
            this.enqueueEvent(event);
            return false;
        }

        if (this.isUsernameInUse(loginEvent.playerName)) {
            final FailureEvent event = new FailureEvent(clientSocketBuffer, "Username is in use.");
            this.enqueueEvent(event);
            return false;
        }

        // Successful login
        return true;
    }

    /**
     * Checks if the newly connected WebSocketBuffer is in a valid state
     * and responding correctly to be used as a client for the game.
     *
     * @param webSocketBuffer The buffer of which to be checked.
     * @return If the new connection is in a valid state for the game.
     */
    private boolean isNewConnectionValid(final WebSocketBuffer webSocketBuffer) {
        // TODO: Refactor WebSocketBuffer#sync to make it understandable.
        return webSocketBuffer.sync();
    }

    /**
     * Creates a new player with the given username.
     * This function does not validate connectivity or username use status.
     *
     * @param username The username of the player to add.
     * @return A newly created player with the given username.
     */
    private Player createNewPlayer(final String username) {
        final int playerID = this.getNextAvailablePlayerID();
        final Player.Type type = this.getNextAvailablePlayerType();
        return new Player(playerID, type, username);
    }

    /**
     * Adds a new player to the game and server.
     * @param player The player to add.
     */
    private void addNewPlayer(final Player player, final WebSocketBuffer clientSocketBuffer) {
        // TODO: Also add player to Server's data.
        this.game.add(player);
    }

    /**
     * @return The next available player type, based on the current players in the game.
     */
    private Player.Type getNextAvailablePlayerType() {
        // TODO: Get proper player count once players are stored in a list/map.
        final int playerCount = -1;
        return Player.Type.getByID(playerCount % Player.Type.values().length);
    }

    /**
     * @return The next available player ID, or -1 if the game is full.
     */
    private int getNextAvailablePlayerID() {
        // TODO: Implement.
        return -1;
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
     * @return If the given username is currently in use by another player.
     */
    private boolean isUsernameInUse(final String username) {
        // TODO: Implement once players are stored in a list/map.
        return false;
    }

    /**
     * TODO:
     * @param event
     */
    private void enqueueEvent(final ServerNetworkEvent event) {
        this.serverEventQueue.push(event);
    }

    @Override
    public void update(final double elapsed) {
        this.game.update(elapsed);
        this.sendUpdatesToClients();
    }

    /**
     * Sends the updated game state to the clients (players).
     */
    private void sendUpdatesToClients() {
        // TODO: Send updated game state to players.
        // This likely means putting Player and WebSocketBuffer into a class
        // which will be mapped to the player's ID.

        // TODO: NetworkEvents should be queued and sent at the end of each game update.
    }

}
