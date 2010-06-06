package com.glgames.game;

import static com.glgames.server.Player.DOWN;
import static com.glgames.server.Player.LEFT;
import static com.glgames.server.Player.RIGHT;
import static com.glgames.server.Player.UP;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

public class Player {
	Object3D model;
	Object2D sprite;
	public int type;
	public String username;

	private int destX, destY;
	private int startX, startY;
	private long startTime;
	private long endTime;

	public int x, y;
	public int deaths;
	public float bubbleAlpha;

	private static BufferedImage bubble = SpriteLoader.getSprite("images/bubble.png");

	public Player(int type, String username) {
		this.type = type;
		model = new Object3D(type);
		sprite = new Object2D(type);
		this.username = username;
	}

/*	public static String toString(int flags) {
		StringBuilder sb = new StringBuilder();
		if((flags & UP)!= 0) sb.append("UP");
		if((flags & DOWN) != 0) sb.append(" DOWN ");
		if((flags & LEFT) != 0) sb.append(" LEFT ");
		if((flags & RIGHT) != 0) sb.append(" RIGHT");
		return sb.toString();
	}*/

	public void draw(Graphics g) {
		if(sprite == null) return;
		sprite.x = x;
		sprite.y = y;
		sprite.draw(g);
		int screenX = sprite.getScreenX(), screenY = sprite.getScreenY();
		g.setColor(Color.red);
		g.setFont(new Font("Arial", Font.PLAIN, 14));
		g.drawString(username, screenX, screenY);
		if(bubbleAlpha > 0.0f) {
			float[] scales = {
					1f, 1f, 1f, bubbleAlpha -= 0.05f };
			float[] offsets = new float[4];
			RescaleOp rop = new RescaleOp(scales, offsets, null);
			
			((Graphics2D) g).drawImage(bubble, rop, screenX, screenY);
		}
	}

	public void updatePos(int newX, int newY, int timeFromNow) {
		startX = x;
		startY = y;
		startTime = System.currentTimeMillis();
		System.out.println("POS IS UPDATED: " + newX + " " + newY + " " + timeFromNow);
		if(timeFromNow < 0) {					// Time of < 0 is used to indicate unmotion
			x = newX;
			y = newY;
		} else {
			endTime = timeFromNow + startTime;
			System.out.println("updated endTime = " + endTime);
			destX = newX;
			destY = newY;
		}
		makeObjectModelAndSpriteCoordinatesConsistentWithPlayerCoordinates();
	}

	public void handleMove() {
		//System.out.println("destX =" + destX  + " destY=" + destY);
		if(endTime < 0) {
			System.out.println("endtime < 0 returning: " + endTime);
			return;
		}
		long now = System.currentTimeMillis();
		if(now >= endTime) {
			System.out.println("endtime already ended stopping: " + now + ", " + endTime);
			endTime = -1;
			return;
		}
		x = (int)(startX + ((destX - startX) * (now - startTime)) / (endTime - startTime));
		y = (int)(startY + ((destY - startY) * (now - startTime)) / (endTime - startTime));
		System.out.println("x=" + x + " y=" + y);
		makeObjectModelAndSpriteCoordinatesConsistentWithPlayerCoordinates();
	}

	private void makeObjectModelAndSpriteCoordinatesConsistentWithPlayerCoordinates() {
		sprite.x = x;
		sprite.y = y;
		model.baseX = x;
		model.baseZ = y;
	}
}