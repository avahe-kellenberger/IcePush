package net.threesided.server;

import net.threesided.shared.InterthreadQueue;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class InternetRelayChat implements Runnable {
    private static final String[] controllers = {
        "_^_", "Tekk", "Evil_", "linkmaster03", "Dezired`"
    };

    // Messages queued by the application to be sent to IRC
    static InterthreadQueue<String> messages = new InterthreadQueue<>();

    // Input sent from IRC to be queued until the application calls processInput()
    private static InterthreadQueue<String> inputs = new InterthreadQueue<>();

    // Kick commands parsed out of processInput() to be returned to the application
    static final InterthreadQueue<String> kicks = new InterthreadQueue<>();

    private static String nick;

    private static Socket socket;
    private static BufferedWriter bufferedWriter;
    private static BufferedReader bufferedReader;
    private static String server;
    private static String channel;
    private static int port;

    static boolean sendWinner = false;

    InternetRelayChat(String socket, int p, String c, String n) {
        InternetRelayChat.server = socket;
        InternetRelayChat.port = p;
        InternetRelayChat.channel = c;
        InternetRelayChat.nick = n;
    }

    public void run() {
        try {
            InternetRelayChat.socket = createTheSocket(server, port);
            InternetRelayChat.bufferedWriter = new BufferedWriter(new OutputStreamWriter(InternetRelayChat.socket.getOutputStream()));
            InternetRelayChat.bufferedReader = new BufferedReader(new InputStreamReader(InternetRelayChat.socket.getInputStream()));

            InternetRelayChat.bufferedWriter.write("NICK " + nick + " \n");
            InternetRelayChat.bufferedWriter.flush();
            InternetRelayChat.bufferedWriter.write("USER IcePush * * :IcePush Client Server\n");
            InternetRelayChat.bufferedWriter.flush();

            String input;
            boolean joined = false;
            while ((input = InternetRelayChat.bufferedReader.readLine()) != null) {
                InternetRelayChat.inputs.push(input);
                if (input.contains("MODE") && !joined) {
                    // Attempt to join channel only after first time umode is set
                    InternetRelayChat.inputs.push(":_^_!triangle@internal PRIVMSG :.join " + InternetRelayChat.channel);
                    joined = true;
                }
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    private Socket createTheSocket(String server, int port) throws java.io.IOException {
        final SSLContext sslContext = SSLTool.disableCertificateValidation();
        return Objects.requireNonNull(sslContext).getSocketFactory().createSocket(server, port);
    }

    // This method runs on the application thread
    public static void processInput() {
        String input;
        while ((input = inputs.pull()) != null) {
            if (input.startsWith("PING")) {
                try {
                    InternetRelayChat.bufferedWriter.write(input.replace("PING", "PONG") + "\n");
                    InternetRelayChat.bufferedWriter.flush();
                    continue;
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
            }
            InternetRelayChat.processLine(input);
        }
    }

    private static void handleCommand(String from, String msg) {
        final String[] args = msg.split(" ");
        boolean auth = false;
        for (final String n : InternetRelayChat.controllers) {
            if (from.toLowerCase().equals(n.toLowerCase())) {
                auth = true;
                break;
            }
        }

        if (!auth) {
            return;
        }

        if (args[0].equals("kick")) {
            if (args.length > 1) {
                InternetRelayChat.kicks.push(args[1]);
            } else {
                InternetRelayChat.sendMessage("Not enough arguments for command");
            }
        }
        if (args[0].equals("join")) {
            if (args.length > 1) {
                try {
                    InternetRelayChat.bufferedWriter.write("JOIN " + args[1] + "\n");
                    InternetRelayChat.bufferedWriter.flush();
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                InternetRelayChat.sendMessage("Not enough arguments for command");
            }
        }
        if (args[0].equals("sendwinner")) {
            if (args.length > 1) {
                try {
                    InternetRelayChat.sendWinner = Boolean.parseBoolean(args[1]);
                } catch (final Exception ex) {
                    InternetRelayChat.sendMessage(ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                InternetRelayChat.sendMessage("Not enough arguments: choices are true or false");
            }
        }
    }

    private static void processLine(String input) {
        try {
            final String[] partsColon = input.split(":");
            final String[] partsSpace = input.split(" ");
            final String cmd = partsSpace[1].toUpperCase();
            if (cmd.equals("PRIVMSG")) {
                final String from = partsSpace[0].split("!")[0].substring(1);
                final String msg = partsColon[partsColon.length - 1];
                if (msg.startsWith(".")) {
                    InternetRelayChat.handleCommand(from, msg.substring(1));
                }
                if (!msg.contains("\u0001")) {
                    InternetRelayChat.messages.push("<" + from + "> " + msg);
                }
            } else if (cmd.equals("KICK") || cmd.equals("INVITE")) {
                InternetRelayChat.bufferedWriter.write("JOIN " + InternetRelayChat.channel + "\n");
                InternetRelayChat.bufferedWriter.flush();
            }
        } catch (final Exception ex) {
            System.out.print("Exception while processing IRC line: ");
            ex.printStackTrace();
        }
    }

    static void sendMessage(String message) {
        try {
            InternetRelayChat.bufferedWriter.write("PRIVMSG " + InternetRelayChat.channel + " :" + message + "\n");
            InternetRelayChat.bufferedWriter.flush();
        } catch (Exception ignored) {}
    }

    public static void logout() throws Exception {
        InternetRelayChat.bufferedWriter.flush();
        InternetRelayChat.bufferedWriter.close();
        InternetRelayChat.bufferedReader.close();
        InternetRelayChat.socket.close();
        InternetRelayChat.bufferedReader = null;
        InternetRelayChat.bufferedWriter = null;
        InternetRelayChat.socket = null;
    }
}
