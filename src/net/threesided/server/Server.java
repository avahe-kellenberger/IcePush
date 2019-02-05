package net.threesided.server;

import net.threesided.server.net.WebSocketBuffer;
import net.threesided.shared.InterthreadQueue;
import net.threesided.util.ThreadedTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final InterthreadQueue<Socket> incomingConnections;
    private final ThreadedTask gameLoopTask, clientAcceptorTask;

    private ServerSocket serverSocket;

    /**
     * @param port The port to which the server will be bound.
     */
    public Server(final int port) {
        this.incomingConnections = new InterthreadQueue<>();

        // Accept all incoming connections.
        this.clientAcceptorTask = new ThreadedTask(this::acceptConnections, () -> true, true, 0);

        // 60 FPS game loop update.
        this.gameLoopTask = new ThreadedTask(() -> {}, () -> true, true, 0.01667);
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
            // TODO: Pass player into `Game`.
            final Player player = new Player(new WebSocketBuffer(socket));
        } catch (final IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Starts the server.
     *
     * @param port The port the server will listen on.
     * @throws IOException If an I/O error occurs when opening the server's socket.
     */
    public void start(final int port) throws IOException {
        this.serverSocket = new ServerSocket(port);

        // Start listening for incoming client connections.
        this.clientAcceptorTask.start();

        // Starts the game update loop task.
        this.gameLoopTask.start();
    }

    /**
     * Signals the server to stop.
     */
    public void stop() {
        this.gameLoopTask.stop();
        this.clientAcceptorTask.stop();
    }

}
