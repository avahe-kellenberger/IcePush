package com.glgames.game;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;

import com.glgames.game.ui.UIComponent;
import com.glgames.shared.Opcodes;

public class Renderer {
	private static final long serialVersionUID = 1L;

	public static final int SOFTWARE_2D = 0;
	public static final int SOFTWARE_3D = 1;
	public static final int HARDWARE_3D = 2;
	public static int GRAPHICS_MODE = SOFTWARE_2D;

	public static String message = "Select a server and username.";

	int selectedButton;
	int mouseOverButton;

	protected Component canvas;
	protected Image backbuffer;
	protected Graphics outgfx;
	protected Graphics bg;
	protected MemoryImageSource memsrc;
	protected Image memimg;

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
	int width, height, pixels[];
	int scaledWidth;


	public Renderer(Component c) {
		canvas = c;
		faceArray = new Face[5000];
		//System.out.println("width: " + c.getWidth() + " height: " + c.getHeight());

		width = IcePush.WIDTH;
		height = IcePush.HEIGHT;
		scaledWidth = width << 12;
	}

	public void initGraphics() {
		pixels = new int[width * height];
		backbuffer = canvas.createImage(IcePush.WIDTH, IcePush.HEIGHT);
		memsrc = new MemoryImageSource(width, height, pixels, 0, width);
		memsrc.setAnimated(true);
		memimg = canvas.createImage(memsrc);
		bg = backbuffer.getGraphics();
		outgfx = canvas.getGraphics();
		canvas.requestFocus();
	}

	public Component getCanvas() {
		return canvas;
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

		drawTopButtons();
	}

	private void drawTopButtons() {
		int w = GameObjects.button.getWidth();
		int x = 28, y = 30;
		for(int i = 0; i < 5; i++) {
			if(i == selectedButton || i == mouseOverButton) {
				bg.drawImage(GameObjects.button2, x, y, null);
			} else {
				bg.drawImage(GameObjects.button, x, y, null);
			}
			x += w;
		}
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
		if(chats_visible)
			drawNewChats();
	}
	
	static boolean chats_visible;
	public static String curChat = "";
	public static ArrayList<String> chats = new ArrayList<String>();
	//private int count;
	
	/*private void drawChats() {
		String chat;

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
	}*/
	
	static Font chatsFont;
	static BufferedImage font;
	static {
		try {
			font = SpriteLoader.getSprite("data/font.png");
			chatsFont = Font.createFont(Font.TRUETYPE_FONT,
					GameObjects.class.getResourceAsStream("/data/dina.ttf")).deriveFont(15.0f);
		} catch (Exception e) {
			e.printStackTrace();
			chatsFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
		}
	}
	static final Color chatsBoxColor = new Color(0, 0, 0, 150);
	private void drawNewChats() {
		String chat;
		
		bg.setColor(chatsBoxColor);
		bg.setFont(chatsFont);
		
		bg.fillRoundRect(50, -21, 700, 200, 50, 50);
		bg.setColor(Color.white);
		bg.drawLine(50, 155, 750, 155);
		
		for(int k = 0; k < chats.size(); k++) {
			chat = chats.get(k);
			bg.drawString(chat, 70, 160 - (chats.size() - k) * 15);
		}
		Player p = GameObjects.players[NetworkHandler.id];
		if(p == null)
			return;
		
		String str = IcePush.is_chat ? "<" + p.username + "> " + curChat + "_" : "<enter> to chat";
		bg.drawString(str, 70, 172);
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
	
	/* these are Bad and Use lots of cpu due to bufferedImage */
	/*private void drawString(String s, int x, int y) {
		for(int k = 0; k < s.length(); k++) {
			char c = s.charAt(k);
			drawCharacter(c, x, y);
			x += 7;
		}
	}
	
	private void drawCharacter(char c, int x, int y) {
		int index = 0;
		if(c < 32)
			return;
		if(c >= '0' && c <= '9')
			index = c - '0' + 1;
		else if(c >= 'A' && c <= 'Z')
			index = c - 'A' + 11;
		else if(c >= 'a' && c <= 'z')
			index = c - 'a' + 37;
		int srcx = index * 7;
		BufferedImage ch = font.getSubimage(srcx, 0, 7, 12);
		bg.drawImage(ch, x, y, null);
	}*/
	
	private void focusCamera() {
		Player p = GameObjects.players[NetworkHandler.id];
		if(p == null)
			return;		
		focusX = focusY = focusZ = 0;
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
		clear();
		doRender(scenery);
		doRender(objArray);
		memsrc.newPixels();
		bg.drawImage(memimg, 0, 0, null);
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
				solidTriangle(t.x1, t.y1, t.x2, t.y2, t.x3, t.y3, t.color.getRGB());
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

	public void drawLoadingErrorIfThereIsSuchAnError() {
		if(GameObjects.error == null)
			return;
		bg.setColor(Color.red);
		bg.setFont(deathsBoxFont);
		bg.drawString(GameObjects.error, 50, 50);
	}

	/**
	 * Sorts a trio of vertices by height so that y1 <= y2 <= y3 Implemented as
	 * its own method with global variables so that triangle code can stay small
	 * (Crucial to fitting within CPU code cache for these presumably highly
	 * performance intensive routines.)
	 */

	private static int _x1, _y1, _x2, _y2, _x3, _y3;

	private static void triSort(int x1, int y1, int x2, int y2, int x3, int y3) {
		_x1 = x1;
		_y1 = y1;
		_x2 = x2;
		_y2 = y2;
		_x3 = x3;
		_y3 = y3;
		int exchx = 0, exchy = 0; // two exchange variables are used to take
		// advantage of potential ILP

		if (_y1 > _y2) { // Each exchange block is dependant on the previous
			// one, so crap
			exchx = _x1;
			exchy = _y1;

			_x1 = _x2;
			_y1 = _y2;

			_x2 = exchx;
			_y2 = exchy;
		}

		if (_y1 > _y3) {
			exchx = _x1;
			exchy = _y1;

			_x1 = _x3;
			_y1 = _y3;

			_x3 = exchx;
			_y3 = exchy;
		}

		if (_y2 > _y3) {
			exchx = _x2;
			exchy = _y2;

			_x2 = _x3;
			_y2 = _y3;

			_x3 = exchx;
			_y3 = exchy;
		}

	}

	/*public void solidTriangle(int X1, int Y1, int X2, int Y2, int X3,
			int Y3, int color) {
		triSort(X1, Y1, X2, Y2, X3, Y3);
		if (_y3 == _y1)
			return;

		int step31 = scaledWidth + ((_x3 - _x1) << 12) / (_y3 - _y1);

		int right = _y1 * scaledWidth, left = right;
		int boundLeft = _y1 * width, boundRight = boundLeft + width;

		if (_y1 != _y2) {
			right += _x1 << 12;
			left = right;
			int step21 = scaledWidth + ((_x2 - _x1) << 12) / (_y2 - _y1);
			// Top part, triangle is broadening. stepLeft < stepRight
			int stepLeft = 0, stepRight = 0;
			if (step21 > step31) {
				stepLeft = step31;
				stepRight = step21;
			} else {
				stepLeft = step21;
				stepRight = step31;
			}
			while (_y1 != _y2) {
				if (_y1 >= 0 && _y1 < height) {
					int l = left >> 12;
					int r = right >> 12;

					if (l < boundLeft)
						l = boundLeft;
					if (r > boundRight)
						r = boundRight;

					while (l < r)
						pixels[l++] = color;
				}
				left += stepLeft;
				right += stepRight;
				boundLeft = boundRight;
				boundRight += width;
				_y1++;
			}
		} else {
			// Triangle is flat topped; adjust left & right accordingly + skip
			// filling top half
			if (_x1 > _x2) {
				right += _x1 << 12;
				left += _x2 << 12;
			} else {
				right += _x2 << 12;
				left += _x1 << 12;
			}
		}

		if (_y2 != _y3) {
			int step23 = scaledWidth + ((_x2 - _x3) << 12) / (_y2 - _y3);
			// Bottom part: Triangle is narrowing. stepLeft > stepRight.
			int stepLeft = 0, stepRight = 0;
			if (step23 > step31) {
				stepLeft = step23;
				stepRight = step31;
			} else {
				stepLeft = step31;
				stepRight = step23;
			}
			while (_y2 != _y3) {
				if (_y2 >= 0 && _y2 < height) {
					int l = left >> 12;
					int r = right >> 12;

					if (l < boundLeft)
						l = boundLeft;
					if (r > boundRight)
						r = boundRight;

					while (l < r)
						pixels[l++] = color;
				}

				left += stepLeft;
				right += stepRight;
				boundLeft = boundRight;
				boundRight += width;
				_y2++;
			}
		}

	//	if(X1 >= 0 && X1 < width && Y1 >= 0 && Y1 < height) pixels[X1 + Y1 * width] = 0xffffffff;
	//	if(X2 >= 0 && X2 < width && Y2 >= 0 && Y2 < height) pixels[X2 + Y2 * width] = 0xffffffff;
	//	if(X3 >= 0 && X3 < width && Y3 >= 0 && Y3 < height) pixels[X3 + Y3 * width] = 0xffffffff;

	}*/
	static final int lineskip = 1;
	public void solidTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int color) {
		float startx, endx;
		float slope21, slope31, slope32;
		float slopeleft, sloperight;
		int temp, off, start, end;
		
		if(y2 < y1) {
			temp = y2;
			y2 = y1;
			y1 = temp;
			temp = x2;
			x2 = x1;
			x1 = temp;
		}
		if(y3 < y1) {
			temp = y3;
			y3 = y1;
			y1 = temp;
			temp = x3;
			x3 = x1;
			x1 = temp;
		}
		if(y3 < y2) {
			temp = y3;
			y3 = y2;
			y2 = temp;
			temp = x3;
			x3 = x2;
			x2 = temp;
		}
		
		if(y1 == y3)
			return;
		
		slope21 = (float) (x2 - x1) / (y2 - y1);
		slope31 = (float) (x3 - x1) / (y3 - y1);
		slope32 = (float) (x3 - x2) / (y3 - y2);
		
		startx = endx = x1;
		if(y1 != y2) {
			if(slope21 > slope31) {
				slopeleft = slope31;
				sloperight = slope21;
			} else {
				slopeleft = slope21;
				sloperight = slope31;
			}
			
			for(int y = y1; y != y2; y++) {
				if(y > 0 && y < height && y % lineskip == 0) {
					off = y * width;
					start = off + (int) startx;
					end = off + (int) endx;
					if(start < off)
						start = off;
					if(end > off + width - 1)
						end = off + width - 1;
				
					while(start <= end)
						pixels[start++] = color;
				}

				startx += slopeleft;
				endx += sloperight;
			}
		} else {
			if (x1 > x2) {
				startx = x2;
				endx = x1;
			} else {
				startx = x1;
				endx = x2;
			}
		}
		
		if(y2 != y3) {
			if(slope32 > slope31) {
				slopeleft  = slope32;
				sloperight = slope31;
			} else {
				slopeleft  = slope31;
				sloperight = slope32;
			}
			
			for(int y = y2; y != y3; y++) {
				if(y > 0 && y < height && y % lineskip == 0) {
					off = y * width;
					start = off + (int) startx;
					end = off + (int) endx;
					if(start < off)
						start = off;
					if(end > off + width - 1)
						end = off + width - 1;
					
					while(start <= end)
						pixels[start++] = color;
				}
				startx += slopeleft;
				endx += sloperight;
			}
		}
		//pixels[y1 * width + x1] = 0xffffff;
		//pixels[y2 * width + x2] = 0xffffff;
		//pixels[y3 * width + x3] = 0xffffff;
	}

	public void clear() {
		int i = width * height;
		for (int j = 0; j < i; j++)
			pixels[j] = 0;

	}

	// THESE STUBS WILL ALL BE REMOVED AS MORE OF THE 2D GRAPHICS LIBRARY IS WRITTEN AND USAGE IS STANDARDIZED WITHIN THE COMPONENT CLASSES

	public void setColor(Color c) {
		bg.setColor(c);
	}

	public void fill3DRect(int a, int b, int c, int d, boolean e) {
		bg.fill3DRect(a, b, c, d, e);
	}

	public void drawString(String a, int b, int c) {
		bg.drawString(a, b, c);
	}

	public int getFontHeight() {
		return bg.getFontMetrics().getHeight();
	}

	public int stringWidth(String s) {
		return bg.getFontMetrics().stringWidth(s);
	}

	public void fillRect(int a, int b, int c, int d) {
		bg.fillRect(a, b, c, d);
	}

	public void drawLine(int a, int b, int c, int d) {
		bg.drawLine(a, b, c, d);
	}
}
