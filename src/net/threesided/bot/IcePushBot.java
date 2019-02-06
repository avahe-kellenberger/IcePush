package net.threesided.bot;

import net.threesided.shared.Vector2D;

import java.util.HashMap;
import java.util.Map;

public class IcePushBot extends Thread {

    public boolean running = true;
    private int id;
    private int angle = -1;
    private int prev_angle = -1;
    private String username;
    private Map<Integer, Player> playerMap = new HashMap<>();

    public IcePushBot(String bot) {
        this.username = bot;
    }

    public static void main(String[] argv) {
        if (argv.length != 1) {
            System.out.println("Missing parameter 'bot name'");
            return;
        }
        new IcePushBot(argv[0]).start();
    }

    public void run() {
        NetworkHandler networking = new NetworkHandler(this);
        if ((id = networking.login(NetworkHandler.DEFAULT_SERVER, username)) == -1) return;

        while (running) {
            networking.pulse();
            if (angle != prev_angle) {
                if (prev_angle != -1) networking.endMoveRequest();
                if (angle != -1) networking.sendMoveRequest(angle);
            }
            prev_angle = angle;
            angle = -1;
            if (playerMap.get(id) != null) {
                Vector2D my_pos = playerMap.get(id).getLocation();
                Player p = getNearestPlayer();
                if (p != null) {
                    Vector2D target_pos = p.getLocation();
                    angle = ((int) getAngle(my_pos.x, target_pos.x, my_pos.y, target_pos.y)) * 2;
                }
            }
        }
    }

    public Player getNearestPlayer() {
        Vector2D my_pos = playerMap.get(id).getLocation();
        double closest_dist = Double.MAX_VALUE;
        Player closest_player = null;
        for (Player p2 : playerMap.values()) {
            if (p2.getUsername().equals(username) || p2.isDead()) continue;
            Vector2D player_pos = p2.getLocation();
            double dist = my_pos.getDistance(player_pos);
            if (dist < closest_dist) {
                closest_dist = dist;
                closest_player = p2;
            }
        }
        return closest_player;
    }

    public void onNewPlayer(int id, String username, int deaths) {
        this.playerMap.put(id, new Player(username));
    }

    public void onMove(int id, int x, int y) {
        final Player player = this.playerMap.get(id);
        player.setLocation(new Vector2D(x, y));
        player.setDead(false);
    }

    public void onChat(String message) {
    }

    public void onDied(int id, int deaths) {
        playerMap.get(id).setDead(true);
        angle = -1;
    }

    public void onLogout(int id) {
        playerMap.remove(id);
    }

    public void stopThread() {
        running = false;
    }

    // an awful way of calculating the angle, make this better
    public double getAngle(double x1, double x2, double y1, double y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1) * 64 / Math.PI;
        angle -= 32;
        if (angle > 0) {
            double prev_angle = angle;
            angle = 128;
            angle -= prev_angle;
            angle = -angle;
        }
        angle = Math.abs(angle);
        return angle;
    }
}
