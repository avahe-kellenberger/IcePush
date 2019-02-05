package net.threesided.game;

import static net.threesided.shared.Constants.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import net.threesided.shared.PacketBuffer;

public class NetworkHandler {
    static String DEFAULT_SERVER = "strictfp.com";
    // The id of the connected player
    public static int id;

    private static Socket sock;
    static PacketBuffer pbuf;
    static long pingTime;
    static boolean DEBUG = false;
    static String message = "";

    public static boolean login(String server, String username) {
        try {
            long start = System.currentTimeMillis();
            sock = new Socket(server, 2345);
            if (DEBUG)
                System.out.println(
                        "Time to establish socket: " + (System.currentTimeMillis() - start));
            sock.setTcpNoDelay(true);

            OutputStream out = sock.getOutputStream();
            InputStream in = sock.getInputStream();

            out.write(0); // connecting client
            out.write(VERSION);
            out.write(username.length());
            out.write(username.getBytes());
            out.flush();

            int result = in.read();
            if (result == FAILURE) {
                int len = in.read() & 0xFF;
                byte[] buf = new byte[len];
                int read = in.read(buf, 0, len);
                if (read < len) {
                    message = "Invalid response from server.";
                    return false;
                } else {
                    message = new String(buf);
                    return false;
                }
            } else if (result == SUCCESS_LOG) {
                // Successful login
                id = in.read();
                pbuf = new PacketBuffer(sock);
                return true;
            } else {
                message = "Invalid response from server.";
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            message = "Error connecting to server: " + e.getMessage();
            return false;
        }
    }

    public static void sendProjectileRequest(int direction) {
        pbuf.beginPacket(PROJECTILE_REQUEST);
        pbuf.writeByte(direction);
        pbuf.endPacket();
    }

    public static void sendChatMessage(String msg) {
        try {
            pbuf.beginPacket(CHAT_REQUEST);
            pbuf.writeString(msg);
            pbuf.endPacket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int moveID;

    public static void sendMoveRequest(int dir) {
        try {
            if (DEBUG)
                System.out.println(
                        "SENDING MOVE REQUEST - ID: "
                                + moveID
                                + " - DIR: "
                                + dir
                                + ", TIME: "
                                + System.currentTimeMillis());
            pbuf.beginPacket(MOVE_REQUEST);
            pbuf.writeByte(dir);
            // pbuf.writeByte(moveID);
            pbuf.endPacket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void endMoveRequest(int moveDir) {
        try {
            if (DEBUG)
                System.out.println(
                        "ENDING MOVE REQUEST - DIR: "
                                + moveDir
                                + ", ID: "
                                + moveID
                                + " - TIME: "
                                + System.currentTimeMillis());
            pbuf.beginPacket(END_MOVE);
            // pbuf.writeByte(moveDir);
            // pbuf.writeByte(moveID);
            pbuf.endPacket();
            // moveID = (moveID + 1) & 255;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void move(int code, boolean end) {
        if (end) endMoveRequest(code);
        else sendMoveRequest(code);
    }

    /*public static void ping() {
        pingTime = System.currentTimeMillis();
        pbuf.beginPacket(PING);
        pbuf.endPacket();
    } */

    public static void logOut() {
        try {
            if (pbuf == null) return;
            pbuf.beginPacket(LOGOUT);
            pbuf.endPacket();
            pbuf.sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
