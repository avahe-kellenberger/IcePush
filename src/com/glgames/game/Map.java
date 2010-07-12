package com.glgames.game;

import com.glgames.shared.FileBuffer;

public class Map {
	public static Map[] maps;
	int width, height, planes;
	int[][][] map;
	
	public static void load() {
		FileBuffer fb = new FileBuffer("maps");
		maps = new Map[fb.readByte()];
		for(int k = 0; k < maps.length; k++) {
			int lenOfMapFile = fb.readInt(); // not really useful for maps because length is known
			Map m = new Map();
			m.planes = fb.readByte();
			m.width = fb.readByte();
			m.height = fb.readByte();
			for(int h = 0; h < m.planes; h++) {
				for(int y = 0; y < m.height; y++) {
					for(int x = 0; x < m.width; x++) {
						m.map[h][y][x] = fb.readByte();
					}
				}
			}
			maps[k] = m;
			fb.debug();
		}
	}
}
