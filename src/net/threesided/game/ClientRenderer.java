package net.threesided.game;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.RenderingHints;

import net.threesided.graphics3d.Object3D;
import net.threesided.graphics3d.Renderer3D;

public class ClientRenderer extends Renderer3D {
    public static final int SOFTWARE_2D = 0;
    public static final int SOFTWARE_3D = 1;
    //public static final int HARDWARE_3D = 2;
    public static int GRAPHICS_MODE = SOFTWARE_2D;
    private int roundTime = -1;
    private int deathTime = -1;
    public String winner;

    int cameraZoom = 64;

    public ClientRenderer(Component c, int w, int h) {
        super(c, w, h);
    }

    public void renderScene(Graphics g, boolean in_chat) {
        if (GRAPHICS_MODE == SOFTWARE_2D) {
            renderScene2D(GameObjects.players, g);
        } else if (GRAPHICS_MODE == SOFTWARE_3D) {
            Object3D objs[] = new Object3D[GameObjects.players.length];
            for (int i = 0; i < objs.length; i++) {
                if (GameObjects.players[i] != null) {
                    objs[i] = GameObjects.players[i].model;
                }
            }
            renderScene3D(objs, GameObjects.scenery);
            drawNames(GameObjects.players, g);
        }
        if (deaths_visible)
            drawDeathsBox(g);
        if (chats_visible)
            drawNewChats(g, in_chat);
        drawRoundTime(g);
        drawDeathTime(g);
    }

    private void drawNewChats(Graphics g, boolean in_chat) {
        String chat;

        g.setColor(chatsBoxColor);
        g.setFont(chatsFont);

        g.fillRoundRect(50, -21, 700, 200, 50, 50);
        g.setColor(Color.white);
        g.drawLine(50, 155, 750, 155);

        for (int k = 0; k < chats.size(); k++) {
            chat = chats.get(k);
            g.drawString(chat, 70, 160 - (chats.size() - k) * 15);
        }
        Player p = GameObjects.players[NetworkHandler.id];
        if (p == null)
            return;

        String str = in_chat ? "<" + p.username + "> " + curChat + "_" : "<enter> to chat";
        g.drawString(str, 70, 172);
    }

    private void drawNames(Player[] players, Graphics g) {
        for (Player p : players)
            if (p != null) {
                Object3D o = p.model;
                double maxy = 0;
                for (int k = 0; k < o.vertY.length; k++)
                    if (o.vertY[k] > maxy)
                        maxy = o.vertY[k];
                double[] pt = transformPoint(o.baseX, o.baseY, o.baseZ,
                        0, maxy + 25, 0);
                int[] scr = worldToScreen(pt[0], pt[1], pt[2]);

                int width = g.getFontMetrics().stringWidth(p.username) / 2;
                g.setFont(namesFont);
                g.setColor(Color.red);
                g.drawString(p.username, scr[0] - width, scr[1]);
            }
    }

    protected void renderScene2D(Player[] players, Graphics g) {
        g.drawImage(GameObjects.background, 0, 0, null);

        for (Player p : players) {
            if (p == null)
                continue;
            p.draw(g);
        }
    }

    private void drawDeathsBox(Graphics g) {
        int x = 210, y = 280;
        g.drawImage(GameObjects.dbox, x, y, null);
        g.setColor(Color.white);
        g.setFont(chatsFont);
        y += 35;
        for (int k = 0; k < GameObjects.players.length; k++) {
            if (GameObjects.players[k] == null) continue;
            Player plr = GameObjects.players[k];
            g.drawString(plr.username + " - " + plr.deaths, x + 25, y += 15);
        }
    }

    public void drawWelcomeScreen(Graphics g) {
        g.drawImage(GameObjects.background, 0, 0, null);
        g.setColor(Color.white);
        g.setFont(titleFont);

        g.setColor(Color.white);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public void updateCamera(int x, int y) {
        cameraX = (cameraZoom * sines[yaw]) + x;
        cameraZ = (cameraZoom * cosines[yaw]) + y;
    }

    public void updateCamera() {
        pitch &= 0xff;
        yaw &= 0xff;
        Player pl = GameObjects.players[NetworkHandler.id];
        if (pl == null) return;
        cameraX = (cameraZoom * sines[yaw]) + pl.sprite.x;
        cameraZ = (cameraZoom * cosines[yaw]) + pl.sprite.y;
    }

    public void setRoundTime(int time) {
        //System.out.println("Time being set to " + time);
        if (roundTime > 0 && time > roundTime) {
            //	System.out.println("time="+time+" roundTime="+roundTime);
            winner = getWinner();
            for (Player p : GameObjects.players)
                if (p != null)
                    p.deaths = 0;
        }
        roundTime = time;
    }

    public void setDeathTime(int time) {
        deathTime = time;
    }

    private String getWinner() {
        int minDeaths = Integer.MAX_VALUE;
        String winner = null;
        int tieCount = 1;
        for (Player p : GameObjects.players) {
            if (p != null) {
                if (p.deaths < minDeaths) {
                    minDeaths = p.deaths;
                    winner = p.username + " HAS WON AND IS NOW THE WINNER";
                    tieCount = 1;
                } else if (p.deaths == minDeaths) {
                    tieCount++;
                    winner = "<" + tieCount + " way tie>";
                }
            }
        }
        return winner;
    }

    private void drawRoundTime(Graphics g) {
        //int timesec = roundTime / 1000;
        if (roundTime < 0) return;                        // Not in any round
        g.setFont(chatsFont);
        g.setColor(Color.white);
        String mins = Integer.toString(roundTime / 60);
        String secs = Integer.toString(roundTime % 60);
        if (secs.length() == 1) secs = '0' + secs;
        String time = "Time remaining: " + mins + ':' + secs;
        FontMetrics fm = g.getFontMetrics();
        int a = fm.getAscent();
        int y = a + 34;
        String str = (winner == null) ? "<No winner>" : winner;
        g.drawString(time, (width - fm.stringWidth(time)) / 2, y);
        g.drawString(str, (width - fm.stringWidth(str)) / 2, y + a + fm.getDescent());
    }

    private void drawDeathTime(Graphics g) {
        //int timesec = roundTime / 1000;
        if (deathTime <= 0) return;                        // Not dead
        g.setFont(chatsFont);
        g.setColor(Color.white);
        String secs = Integer.toString(deathTime % 60);
        String time = "Time until respawn: " + secs + (deathTime % 60 == 1 ? " sec" :" secs");
        FontMetrics fm = g.getFontMetrics();
        int a = fm.getAscent();
        int y = a + 64;
        g.drawString(time, (width - fm.stringWidth(time)) / 2, y);
    }
}
