package com.glgames.game;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Object2D extends GameObject {
	public int x, y, width, height;

	private BufferedImage sprite;

	public Object2D(String spriteName, int type) {
		super(type);
		sprite = SpriteLoader.getSprite(spriteName);

		width = sprite.getWidth();
		height = sprite.getHeight();
	}

	public void draw(Graphics g) {
		g.drawImage(sprite, getScreenX(), getScreenY(), null);
	}

	public int getScreenX() {
		return GameObjects.playingArea.x + (this.x >> 1);
	}

	public int getScreenY() {
		return GameObjects.playingArea.y + (this.y >> 1);
	}
}
