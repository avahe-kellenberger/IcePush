package net.threesided.server;

import net.threesided.server.net.WebSocketBuffer;
import net.threesided.shared.InterthreadQueue;
import net.threesided.util.LoopedThreadedTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public class Server {

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
        this.gameLoopTask = new LoopedThreadedTask(this.game::update, serverRunCondition, frameDurationSeconds);
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
            this.game.add(new Player(new WebSocketBuffer(socket)));
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

}
