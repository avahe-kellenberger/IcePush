package com.glgames.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Coordinates in this class are relative to the playingArea
 * rectangle in the GameObjects class.
 * They are transformed prior to rendering.
 * 
 * Implementation of movement is mostly serverside.
 * @author
 */
public class GamePlayer {
	public String username;
	public int deaths;
	public boolean canMove = true;
	public Rectangle area;
	
	private BufferedImage sprite;
	
	public GamePlayer(String spriteName) {
		sprite = SpriteLoader.getSprite(spriteName);
		area = new Rectangle();
		area.width = sprite.getWidth();
		area.height = sprite.getHeight();
	}
	
	public void draw(Graphics g) {
		// generate proper coordinates for drawing
		int screenX = GameObjects.playingArea.x + this.area.x;
		int screenY = GameObjects.playingArea.y + this.area.y;
		//g.setColor(Color.red);
		//g.drawRect(screenX, screenY, area.width, area.height);
		g.setColor(Color.red);
		g.setFont(new Font("Arial", Font.PLAIN, 14));
		g.drawString(username, screenX, screenY);
		g.drawImage(sprite, screenX, screenY, null);
		if(!canMove) {
			g.setColor(new Color(0, 100, 255, 100));
			g.fillOval(screenX, screenY, area.width, area.height);
		}
	}
}
