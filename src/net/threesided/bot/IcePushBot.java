package net.threesided.bot;

public class IcePushBot extends Thread {

    public static final String NAME = "bot";

    public boolean running = true;
    private int id;
    private NetworkHandler networking;

    public static void main(String[] argv) {
        new IcePushBot().start();
    }

    public void run() {
        networking = new NetworkHandler(this);
        if ((id = networking.login(NetworkHandler.DEFAULT_SERVER, NAME)) == -1)
            return;
        networking.start();

        // TODO bot logic
        while (running) {

        }
    }

    // handling functions for various events
    public void onNewPlayer(int charId, String username, int deaths) {

    }

    public void onMove(int id, int x, int y) {

    }

    public void onChat(String message) {

    }

    public void onDied(int id, int deaths) {

    }

    public void onLogout(int id) {

    }

    public void stopThread() {
        running = false;
    }

}