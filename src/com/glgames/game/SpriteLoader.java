package com.glgames.game;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class SpriteLoader {
	private static Map<String, BufferedImage> sprites;
	
	/**
	 * Gets the sprite with the specified name. If the sprite does not exist in
	 * the internal map, it is loaded from the file.
	 * 
	 * @param name
	 *			The name of the sprite to get
	 * @return A BufferedImage of the sprite, or null if an error occured
	 *		 loading it.
	 */
	public static BufferedImage getSprite(String name) {
		if(sprites.get(name) == null)
			sprites.put(name, loadSprite(name));
		return sprites.get(name);
	}
	
	private static BufferedImage loadSprite(String name) {
		try {
			return ImageIO.read(GameObjects.class.getResource("/" + name));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static {
		sprites = new HashMap<String, BufferedImage>();
	}
}
