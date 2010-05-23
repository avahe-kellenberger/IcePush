package com.glgames.game;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Texture {
	public int sidelen;
	public int[] pixels;

	public Texture(String filename) {
		try {
			BufferedImage im = ImageIO.read(new File(filename));
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
