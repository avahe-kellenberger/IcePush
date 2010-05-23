package com.glgames.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

/**
 * Coordinates in this class are relative to the playingArea
 * rectangle in the GameObjects class.
 * They are transformed prior to rendering.
 * 
 * Implementation of movement is mostly serverside.
 * @author
 */
public class Player2D extends Object2D {
	public String username;
	public int deaths;
	public float bubbleAlpha;
	public int rotation;

	private static BufferedImage bubble = SpriteLoader.getSprite("images/bubble.png");
	
	public Player2D(String spriteName, int type) {
		super(spriteName, type);
	}
	
	public void draw(Graphics g) {
		super.draw(g);
		int screenX = getScreenX(), screenY = getScreenY();
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
}
