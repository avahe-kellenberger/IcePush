package com.glgames.game;

import static com.glgames.shared.Opcodes.TREE;

import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

public abstract class Renderer {
	private static final long serialVersionUID = 1L;

	public static String message = "Select a server and username.";

	protected Component canvas;
	protected Image backbuffer;
	protected Graphics outgfx;
	protected Graphics bg;

	public Renderer(Component c) {
		canvas = c;
	}

	public void initGraphics() {
		backbuffer = canvas.createImage(IcePush.WIDTH, IcePush.HEIGHT);
		bg = backbuffer.getGraphics();
		outgfx = canvas.getGraphics();
		canvas.requestFocus();
	}

	public Component getCanvas() {
		return canvas;
	}

	public void drawLoadingBar(String s, int p) {
		int width = 400, height = 30, x = IcePush.WIDTH / 2 - width / 2;

		bg.setColor(Color.cyan);
		bg.drawRect(x, IcePush.HEIGHT / 2 - height / 2, width, height);

		x += 2;
		if (p == -1)
			width -= 2;
		else
			width = (int) (p / 100.0d * width);
		height -= 3;

		if (p == -1)
			bg.setColor(new Color(150, 0, 0));
		else
			bg.setColor(new Color(0, 0, 150));
		bg.fillRect(x, IcePush.HEIGHT / 2 - height / 2, width, height);

		x = IcePush.WIDTH / 2 - bg.getFontMetrics().stringWidth(s) / 2;
		bg.setColor(Color.white);
		bg.drawString(s, x, IcePush.HEIGHT / 2
				- bg.getFontMetrics().getHeight() / 2 + 12);
	}

	public void drawWelcomeScreen(int cycle) {
		int w;
		double frequency = 0.01d;
		int green = (int) (Math.sin(frequency * cycle % 32 + 2) * 63 + 160);
		int blue = (int) (Math.sin(frequency * cycle % 32 + 4) * 63 + 160);

		Color col = new Color(0, green, blue);
		bg.setColor(col);
		bg.fillRect(0, 0, IcePush.WIDTH, IcePush.HEIGHT);
		bg.drawImage(GameObjects.logo, 50, 50, null);

		bg.setColor(Color.white);
		bg.setFont(new Font("Arial", Font.PLAIN, 20));

		int y = 190;
		for (String s : GameObjects.instructions) {
			w = bg.getFontMetrics().stringWidth(s);
			bg.drawString(s, IcePush.WIDTH / 2 - w / 2, y += 30);
		}

		bg.setColor(Color.white);
		w = bg.getFontMetrics().stringWidth(message);
		bg.drawString(message, IcePush.WIDTH / 2 - w / 2, 310);

		if (GameObjects.serverMode == GameObjects.TYPE_IN_BOX)
			GameObjects.serverBox.draw(bg);
		else
			GameObjects.serverList.draw(bg);
		GameObjects.usernameBox.draw(bg);

		Rectangle login = GameObjects.loginButton;
		bg.setColor(Color.gray);
		bg.fill3DRect(login.x, login.y, login.width, login.height, true);
		bg.setColor(Color.white);
		w = bg.getFontMetrics().stringWidth("Login");
		bg.drawString("Login", login.x + login.width / 2 - w / 2, login.y + 18);
	}

	public void drawDiedScreen(int l) {
		int alpha = (int) ((l / 50.0d) * 255.0d);
		bg.setColor(new Color(0, 0, 0, alpha));
		bg.fillRect(0, 0, IcePush.WIDTH, IcePush.HEIGHT);
		((Graphics2D) bg).setPaint(new GradientPaint(200, 200, new Color(0,
				255, 0), 400, 400, new Color(0, 0, 255)));
		bg.setFont(new Font("Arial Black", Font.PLAIN, 36));
		bg.drawString("TRY AGAIN", IcePush.WIDTH / 2 - 110,
				IcePush.HEIGHT / 2);
	}

	public void clearScreen() {
		bg.setColor(Color.BLACK);
		bg.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	public void swapBuffers() {
		if (outgfx == null || backbuffer == null)
			return;
		outgfx.drawImage(backbuffer, 0, 0, null);
	}

	public Graphics getBufferGraphics() {
		return bg;
	}

	public void switchMode(int mode) {
		if (mode == GameObjects.GRAPHICS_MODE)
			return;

		if (mode == GameObjects.SOFTWARE_3D || mode == GameObjects.HARDWARE_3D) {
			Player2D[] oldplayers = (Player2D[]) GameObjects.players;
			Object2D[] oldscenery = (Object2D[]) GameObjects.scenery;

			Player3D[] newplayers = new Player3D[oldplayers.length];
			Object3D[] newscenery = new Object3D[oldscenery.length];

			for (int k = 0; k < oldplayers.length; k++) {
				if (oldplayers[k] == null)
					continue;

				newplayers[k] = new Player3D(oldplayers[k].type);
				newplayers[k].baseX = oldplayers[k].x;
				newplayers[k].baseZ = oldplayers[k].y;
				newplayers[k].username = oldplayers[k].username;
				newplayers[k].deaths = oldplayers[k].deaths;
				newplayers[k].rotationY = oldplayers[k].rotation;
			}
			
			newscenery[0] = new Object3D.Cube(400);

			GameObjects.players = newplayers;
			GameObjects.scenery = newscenery;
			Renderer3D r = new Renderer3D(canvas);
			r.focusCamera((int) newplayers[NetworkHandler.id].baseX,
					(int) newplayers[NetworkHandler.id].baseZ);

			IcePush.setRenderer(r);
			GameObjects.GRAPHICS_MODE = GameObjects.SOFTWARE_3D;
		} else if (mode == GameObjects.SOFTWARE_2D) {
			Player3D[] oldplayers = (Player3D[]) GameObjects.players;
			Object3D[] oldscenery = (Object3D[]) GameObjects.scenery;

			Player2D[] newplayers = new Player2D[oldplayers.length];
			Object2D[] newscenery = new Object2D[oldscenery.length];

			for (int k = 0; k < oldplayers.length; k++) {
				if (oldplayers[k] == null)
					continue;

				newplayers[k] = new Player2D(
						oldplayers[k].type == TREE ? "images/tree.png"
								: "images/snowman.png", oldplayers[k].type);
				newplayers[k].x = (int) oldplayers[k].baseX;
				newplayers[k].y = (int) oldplayers[k].baseZ;
				newplayers[k].username = oldplayers[k].username;
				newplayers[k].deaths = oldplayers[k].deaths;
				newplayers[k].rotation = oldplayers[k].rotationY;
			}

			for (int k = 0; k < oldscenery.length; k++) {
				if (oldscenery[k] == null)
					continue;

				newscenery[k] = new Object2D(
						oldscenery[k].type == TREE ? "images/tree.png"
								: "images/snowman.png", oldscenery[k].type);
				newscenery[k].x = (int) oldscenery[k].baseX;
				newscenery[k].y = (int) oldscenery[k].baseZ;
			}

			GameObjects.players = newplayers;
			GameObjects.scenery = newscenery;

			IcePush.setRenderer(new Renderer2D(canvas));
			GameObjects.GRAPHICS_MODE = GameObjects.SOFTWARE_2D;
		} else
			throw new IllegalStateException("wtf");
		if(IcePush.DEBUG)
			System.out.println("Graphics mode set to " + GameObjects.GRAPHICS_MODE);
	}

	public abstract void drawDebug();
}
