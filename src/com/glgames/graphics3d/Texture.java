package com.glgames.graphics3d;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class Texture {
	public int sidelen;
	public int[] pixels;

	public Texture(String filename) {
		try {
			BufferedImage im = ImageIO.read(Texture.class.getResource("/"
					+ filename));
			sidelen = im.getWidth();
			if (sidelen != im.getHeight())
				throw new IllegalStateException("non-square texture");
			pixels = new int[sidelen * sidelen];
			for (int x = 0; x < sidelen; x++)
				for (int y = 0; y < sidelen; y++)
					pixels[y * sidelen + x] = im.getRGB(x, y);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}