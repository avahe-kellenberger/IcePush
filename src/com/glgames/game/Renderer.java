package com.glgames.game;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;

import com.glgames.shared.Opcodes;

public class Renderer {
	private static final long serialVersionUID = 1L;

	public static final int SOFTWARE_2D = 0;
	public static final int SOFTWARE_3D = 1;
	public static final int HARDWARE_3D = 2;
	public static int GRAPHICS_MODE = SOFTWARE_2D;

	public static String message = "Select a server and username.";

	protected Component canvas;
	protected Image backbuffer;
	protected Graphics outgfx;
	protected Graphics bg;

	protected Font titleFont = new Font("Arial", Font.PLAIN, 20);
	protected Font deathsBoxFont = new Font("Arial", Font.PLAIN, 24);
	protected Font debugFont = new Font(Font.DIALOG, Font.PLAIN, 9);
	protected Font diedScreenFont = new Font("Arial Black", Font.PLAIN, 36);
	protected Font namesFont = new Font(Font.DIALOG, Font.PLAIN, 12);

	// Only used in 3D mode
	public double cameraX = 0.0;
	public double cameraY = -100.0;
	public double cameraZ = -450.0;

	public int pitch = 270, yaw = 180;

	public double focusX, focusY, focusZ;

	private Face faceArray[];
	private int faceIndex;

	private double yawSin, yawCos, pitchSin, pitchCos;

	public Renderer(Component c) {
		canvas = c;
		faceArray = new Face[5000];
		Triangles.init(IcePush.WIDTH, IcePush.HEIGHT, c);
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
		((Graphics2D) bg).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		bg.drawString(s, x, IcePush.HEIGHT / 2
				- bg.getFontMetrics().getHeight() / 2 + 12);
	}

	public void drawWelcomeScreen(int cycle) {
		//background(cycle);
		bg.drawImage(GameObjects.background, 0, 0, null);
		int w;
		bg.setColor(Color.white);
		bg.setFont(titleFont);

		int y = 140;
		if(GameObjects.serverList != null) y -= GameObjects.serverList.height;
		for (String s : GameObjects.instructions) {
			w = bg.getFontMetrics().stringWidth(s);
			bg.drawString(s, IcePush.WIDTH / 2 - w / 2, y += 30);
		}

		bg.setColor(Color.white);
		((Graphics2D) bg).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		w = bg.getFontMetrics().stringWidth(message);
		bg.drawString(message, IcePush.WIDTH / 2 - w / 2, GameObjects.serverList == null ? 250 : 155);

		if (GameObjects.serverMode == GameObjects.TYPE_IN_BOX)
			GameObjects.serverBox.draw(bg);
		else if (GameObjects.serverMode == GameObjects.LIST_FROM_SERVER)
			GameObjects.serverList.draw(bg);

		GameObjects.usernameBox.draw(bg);

		button(GameObjects.loginButton, "Login");
		button(GameObjects.helpButton, "Help");
	}

	public void renderScene() {
		if(GRAPHICS_MODE == SOFTWARE_2D) {
			renderScene2D(GameObjects.players);
		}
		if(GRAPHICS_MODE == SOFTWARE_3D) {
			//if(focusX == 0 && focusY == 0 && focusZ == 0)
				focusCamera();
			Object3D objs[] = new Object3D[GameObjects.players.length];
			for(int i = 0; i < objs.length; i++) {
				if(GameObjects.players[i] != null) {
					objs[i] = GameObjects.players[i].model;
				}
			}
			renderScene3D(objs, GameObjects.scenery);
			drawNames(GameObjects.players);
		}
		drawDeathsBox();
		drawChats();
	}
	
	public static String curChat = "<enter> to chat";
	private ArrayList<String> chats = new ArrayList<String>();
	private int count;
	
	private void drawChats() {
		String chat;
		while((chat = InternetRelayChat.msgs.pull()) != null)
			chats.add(chat);
		
		bg.setFont(debugFont);
		for(int k = 0; k < chats.size(); k++) {
			chat = chats.get(k);
			bg.drawString(chat, 35, 430 - (chats.size() - k) * 15);
		}
		
		bg.drawString(curChat, 35 + 3, 440);
		if(count++ % 50 > 25) {
			int width = bg.getFontMetrics().stringWidth(curChat) + 5;
			bg.drawLine(35 + width, 440 - 8, 35 + width, 440);
		}
	}

	private void drawNames(Player[] players) {
		for(Player p : players) if(p != null) {
			Object3D o = p.model;
			int height_variable_based_on_type = 0;
			if(p.type == Opcodes.TREE) {
				height_variable_based_on_type = 120;
			} else if(p.type == Opcodes.SNOWMAN) {
				height_variable_based_on_type = 50;
			}
			double[] pt = transformPoint(o.baseX, o.baseY, o.baseZ,
					-HALF_GAME_FIELD_WIDTH, height_variable_based_on_type, -HALF_GAME_FIELD_HEIGHT);
			int[] scr = worldToScreen(pt[0], pt[1], pt[2]);
			
			int width = bg.getFontMetrics().stringWidth(p.username) / 2;
			bg.setFont(namesFont);
			bg.setColor(Color.red);
			bg.drawString(p.username, scr[0] - width, scr[1]);
		}
	}
	
	private void focusCamera() {
		Player p = GameObjects.players[NetworkHandler.id];
		if(p == null)
			return;		
		focusX = focusY = focusZ = 0;
	}

	private void button(Rectangle r, String text) {
		bg.setColor(Color.gray);
		bg.fill3DRect(r.x, r.y, r.width, r.height, true);
		bg.setColor(Color.white);
		int w = bg.getFontMetrics().stringWidth(text);
		bg.drawString(text, r.x + r.width / 2 - w / 2, r.y + 18);
	}

	public void drawHelpScreen(int cycle) {
		//background(cycle);
		bg.drawImage(GameObjects.background, 0, 0, null);
		int w;
		bg.setColor(Color.white);
		bg.setFont(titleFont);
		((Graphics2D) bg).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		int y = 190;
		for (String s : GameObjects.help) {
			w = bg.getFontMetrics().stringWidth(s);
			bg.drawString(s, IcePush.WIDTH / 2 - w / 2, y += 30);
		}

		button(GameObjects.backButton, "Back");
	}
	
	private void renderScene2D(Player[] players) {
		Graphics2D g = (Graphics2D) bg;
		if(g == null)
			return;
		g.drawImage(GameObjects.background, 0, 0, null);
		
		for (int k = 0; k < players.length; k++) {
			Player p = players[k];
			if (p == null)
				continue;
			p.draw(g);
		}
	}

	private void renderScene3D(Object3D objArray[], Object3D[] scenery) {
		Triangles.setAllPixelsToZero();
		doRender(scenery);
		doRender(objArray);
		Triangles.pm.draw(0, 0, bg);
		drawDebug();
	}
	
	private void drawDeathsBox() {
		bg.setColor(Color.white);
		bg.setFont(deathsBoxFont);
		int x = 200, y = 340;
		bg.drawString("Deaths", x, y);
		bg.drawRect(x, y += 5, 400, 100);
		for (int k = 0; k < GameObjects.players.length; k++) {
			if (GameObjects.players[k] == null) continue;
			Player plr = GameObjects.players[k];
			bg.drawString(plr.username + " - " + plr.deaths, x + 15, y += 20);
		}
	}
	
	static final int HALF_GAME_FIELD_WIDTH = (744 / 2);
	static final int HALF_GAME_FIELD_HEIGHT = (422 / 2);

	private void doRender(Object3D[] objArray) {
		faceIndex = 0;
		while (pitch < 0)
			pitch += 360;
		while (pitch > 360)
			pitch -= 360;
		
		for (Object3D obj : objArray) {
			if (obj == null)
				continue;
			
			double yawRad = Math.toRadians(yaw);
			yawSin = Math.sin(yawRad);
			yawCos = Math.cos(yawRad);

			double pitchRad = Math.toRadians(pitch);
			pitchSin = Math.sin(pitchRad);
			pitchCos = Math.cos(pitchRad);

			int vertexCount;
			for (int currentFace = 0; currentFace < obj.faceVertices.length; currentFace++) {
				boolean withinViewport = false;
				if (obj.faceVertices[currentFace] == null)
					continue;

				vertexCount = obj.faceVertices[currentFace].length;

				double faceCenterX = 0;
				double faceCenterY = 0;
				double faceCenterZ = 0;

				// Will be discarded if this face is culled
				int drawXBuf[] = new int[vertexCount];
				int drawYBuf[] = new int[vertexCount];
				int drawZBuf[] = new int[vertexCount];

				for (int currentVertex = 0; currentVertex < vertexCount; currentVertex++) {
					int vertexID = obj.faceVertices[currentFace][currentVertex];

					double[] transformed = transformPoint(obj.baseX
							- HALF_GAME_FIELD_WIDTH, obj.baseY, obj.baseZ
							- HALF_GAME_FIELD_HEIGHT, obj.vertX[vertexID],
							obj.vertY[vertexID], obj.vertZ[vertexID]);

					obj.vertXRelCam[vertexID] = transformed[0];
					obj.vertYRelCam[vertexID] = transformed[1];
					obj.vertZRelCam[vertexID] = transformed[2];

					if (obj.vertZRelCam[vertexID] <= 0)
						obj.vertZRelCam[vertexID] = 1;

					int[] screen = worldToScreen(obj.vertXRelCam[vertexID],
							obj.vertYRelCam[vertexID],
							obj.vertZRelCam[vertexID]);

					obj.screenX[vertexID] = screen[0];
					obj.screenY[vertexID] = screen[1];

					faceCenterX += obj.vertXRelCam[vertexID];
					faceCenterY += obj.vertYRelCam[vertexID];
					faceCenterZ += obj.vertZRelCam[vertexID];

					int drawX = obj.screenX[vertexID];
					int drawY = obj.screenY[vertexID];
					int drawZ = (int) (1 / obj.vertZRelCam[vertexID]);

					if (drawX >= 0 && drawX <= IcePush.WIDTH && drawY >= 0
							&& drawY <= IcePush.HEIGHT)
						withinViewport = true;

					drawXBuf[currentVertex] = drawX;
					drawYBuf[currentVertex] = drawY;
					drawZBuf[currentVertex] = drawZ;
				}

				if (!withinViewport)
					continue;

				faceCenterX /= vertexCount;
				faceCenterY /= vertexCount;
				faceCenterZ /= vertexCount;

				double distance = faceCenterX * faceCenterX + faceCenterY
						* faceCenterY + faceCenterZ * faceCenterZ;

				if (faceIndex > 4998)
					faceIndex = 4998;
				if (obj.faceColors != null) {
					faceArray[faceIndex++] = new Face(drawXBuf, drawYBuf,
							drawZBuf, vertexCount, distance,
							obj.faceColors[currentFace], null);
				} else if (obj.faceTextures != null) {
					faceArray[faceIndex++] = new Face(drawXBuf, drawYBuf,
							drawZBuf, vertexCount, distance, null,
							obj.faceTextures[currentFace]);
				}
			}
		}
		Triangle[] tris = triangulatePolygons(faceArray, faceIndex);
		java.util.Arrays.sort(tris, 0, triLen);

		for (int i = triLen - 1; i >= 0; i--) {
			Triangle t = tris[i];
			if(t.color != null)
				Triangles.solidTriangle(t.x1, t.y1, t.x2, t.y2, t.x3, t.y3, t.color.getRGB());
		//	else if(t.texture != null)		THIS CODE DOES NOT WORK AND INSTEAD GOES INTO AN INFINITE LOOP
		//		Triangles.textureMappedTriangle(t.texture, t.x1, t.y1, t.x2,
		//				t.y2, t.x3, t.y3, 0, 0, t.z1, 255, 0, t.z2, 255, 255,
		//				t.z3);
		}
	}
	
	static int triLen;

	private Triangle[] triangulatePolygons(Face[] faces, int len) {
		Triangle[] out = new Triangle[faceIndex * (6 - 2)];
		int num = 0;
		for (int k = 0; k < len; k++) {
			Face f = faces[k];
			int fanX = f.drawX[0];
			int fanY = f.drawY[0];

			// Skip the adjacent vertices
			for (int vertex = 2; vertex < f.drawX.length; vertex++) {
				Triangle t = new Triangle();
				t.x1 = fanX;
				t.y1 = fanY;
				t.x2 = f.drawX[vertex - 1];
				t.y2 = f.drawY[vertex - 1];
				t.x3 = f.drawX[vertex];
				t.y3 = f.drawY[vertex];
				
				t.distance = f.distance;
				t.color = f.color;
				t.texture = f.texture;
				out[num++] = t;
			}
		}
		triLen = num;
		return out;
	}

	public void drawDebug() {
		if (bg == null)
			return;
		bg.setColor(Color.white);
		bg.setFont(debugFont);
		bg.drawString("3D Renderer - Camera X: " + cameraX + ", Y: " + cameraY
				+ ", Z: " + cameraZ + ", Pitch: " + pitch + ", Yaw: " + yaw,
				15, 15);
	}

	public double[] transformPoint(double objBaseX, double objBaseY,
			double objBaseZ, double vertX, double vertY, double vertZ) {
		double absVertX = objBaseX + vertX;
		double absVertY = objBaseY + vertY;
		double absVertZ = objBaseZ + vertZ;
		// System.out.println(absVertX + " " + absVertY + " " + absVertZ);
		absVertX -= focusX;
		absVertY -= focusY;
		absVertZ -= focusZ;

		/* Rotation about Y axis -- Camera Yaw */

		double rotated_Y_AbsVertX = (absVertX * yawCos - absVertZ * yawSin);
		double rotated_Y_AbsVertZ = (absVertX * yawSin + absVertZ * yawCos);

		/* Rotation about X axis -- Camera Pitch */

		double rotated_X_AbsVertY = (absVertY * pitchCos - rotated_Y_AbsVertZ
				* pitchSin);
		double rotated_X_AbsVertZ = (absVertY * pitchSin + rotated_Y_AbsVertZ
				* pitchCos);

		rotated_Y_AbsVertX += focusX;
		rotated_X_AbsVertY += focusY;
		rotated_X_AbsVertZ += focusZ;

		return new double[] { rotated_Y_AbsVertX - cameraX,
				rotated_X_AbsVertY - cameraY, rotated_X_AbsVertZ - cameraZ };
	}

	public int[] worldToScreen(double x, double y, double z) {
		int[] ret = new int[2];
		int sW = IcePush.WIDTH / 2, sH = IcePush.HEIGHT / 2;

		ret[0] = sW - (int) (sW * x / z); // Fix for bug #433299297: Left and
											// right are transposed
		ret[1] = sH - (int) (sH * y / z);
		return ret;
	}


	public void drawDiedScreen(int l) {
		int alpha = (int) ((l / 50.0d) * 255.0d);
		bg.setColor(new Color(255, 255, 255, alpha));
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
}
