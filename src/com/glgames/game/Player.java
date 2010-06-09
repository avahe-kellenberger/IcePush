package com.glgames.game;

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

	// CODE IN COMMENTS HAS BEEN COMMENTED OUT
	//private int destX, destY;
	//private int startX, startY;
	//private long startTime;
	//private long endTime;

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

	// ALL CODE IN COMMENTS HAS BEEN COMMENTED OUT
	/*public void updatePos(int newX, int newY, int timeFromNow) {
		startX = x;
		startY = y;
		startTime = System.currentTimeMillis();
		if(timeFromNow < 0) {					// Time of < 0 is used to indicate unmotion
			x = newX;
			y = newY;
			endTime = timeFromNow;
		} else {
			endTime = timeFromNow + startTime;
			destX = newX;
			destY = newY;
		}
		makeObjectModelAndSpriteCoordinatesConsistentWithPlayerCoordinates();
	}

	public void handleMove() {
		if(endTime < 0) {
			return;
		}
		long now = System.currentTimeMillis();
		if(now >= endTime) {
			System.out.println("endtime has arrive, stopping: " + now + ", " + endTime);
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
	}*/
}