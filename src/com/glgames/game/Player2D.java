package com.glgames.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import static com.glgames.server.Player.*;

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
	public int moveflags;
	public int dx, dy;
	public int deaths;
	public float bubbleAlpha;

	private static BufferedImage bubble = SpriteLoader.getSprite("images/bubble.png");
	
	public Player2D(String spriteName) {
		super(spriteName);
	}

	public static String toString(int flags) {
		StringBuilder sb = new StringBuilder();
		if((flags & UP)!= 0) sb.append("UP");
		if((flags & DOWN) != 0) sb.append(" DOWN ");
		if((flags & LEFT) != 0) sb.append(" LEFT ");
		if((flags & RIGHT) != 0) sb.append(" RIGHT");
		return sb.toString();
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
	
	public void setBit(int flag) {
		moveflags |= flag;
	}

	public void clearBit(int flag) {
		moveflags &= ~flag;
	}

	public boolean isSet(int flag) {
		return (moveflags & flag) > 0;
	}

	public void handleMove() {
		if(isSet(UP)) {
			dy--;
		} if(isSet(DOWN)) {
			dy++;
		} else {
			dy = 0;
		}

		if(isSet(LEFT)) {
			dx--;
		} else if(isSet(RIGHT)) {
			dx++;
		} else {
			dx = 0;
		}
		if(dx > 4)
			dx = 4;
		if(dy > 4)
			dy = 4;
		if(dx < -4)
			dx = -4;
		if(dy < -4)
			dy = -4;

		x += dx;
		y += dy;

		//System.out.println("dx = " + dx + " dy = " + dy);
	}
}