package net.threesided.server;

import net.threesided.server.net.WebSocketBuffer;
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
            this.incomingConnections.push(socket);
            this.processIncomingConnection(socket);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Processes the newly connected socket.
     * @param socket The client socket to process after a connection is established.
     */
    private void processIncomingConnection(final Socket socket) {
        try {
            socket.setTcpNoDelay(true);
            final WebSocketBuffer clientSocketBuffer = new WebSocketBuffer(socket);

            if (!this.isNewConnectionValid(clientSocketBuffer)) {
                return;
            }

            final int clientVersionNumber = clientSocketBuffer.readByte();
            if (clientVersionNumber != Constants.VERSION) {
                // TODO: Enqueue failure event to client.
                return;
            }

            final String clientUsername = clientSocketBuffer.readString();
            if (this.isUsernameInUse(clientUsername)) {
                // TODO: Enqueue failure event to client.
                return;
            }

            if (this.isGameFull()) {
                // TODO: Enqueue failure event to client.
                return;
            }

            final Player player = this.createNewPlayer(clientUsername);
            this.processNewPlayer(player);

            // TODO: Enqueue login success event to player
            // TODO: Enqueue player logged in event to players

            // TODO: Set player's position, which should automatically trigger move events

            // TODO: Notify new player of the current round or victory lap with time remaining.
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
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
    private void processNewPlayer(final Player player) {
        // TODO: Also add player to Server's data.
        this.game.add(player);
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
        if (webSocketBuffer.sync()) {
            final int opcode = webSocketBuffer.openPacket();
            // TODO: (Taken from legacy code) Why zero?
            return opcode == 0;
        }
        return false;
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
