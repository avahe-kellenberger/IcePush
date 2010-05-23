package com.glgames.game;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Object2D extends GameObject {
	public int x, y, width, height;
	public int rotation;

	private BufferedImage sprite;

	public Object2D(String spriteName, int type) {
		super(type);
		sprite = SpriteLoader.getSprite(spriteName);

		width = sprite.getWidth();
		height = sprite.getHeight();
	}

	public void draw(Graphics _g) {
		Graphics2D g = (Graphics2D) _g;
		AffineTransform old = g.getTransform();
		g.rotate(Math.toRadians(rotation), getScreenX() + width / 2,
				getScreenY() + height / 2);
		g.drawImage(sprite, getScreenX(), getScreenY(), null);
		g.setTransform(old);
	}

	public int getScreenX() {
		return GameObjects.playingArea.x + this.x;
	}

	public int getScreenY() {
		return GameObjects.playingArea.y + this.y;
	}
}
