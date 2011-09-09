package net.threesided.server;

import java.awt.geom.Path2D;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class MapClass {
	ArrayList<Path2D> paths = new ArrayList<Path2D>();

	Path2D currentPath = createDefaultPath();

	public void SEND_MAP_REQUEST(Player p, byte mapBytes[]) {
		try {
			Path2D path = (Path2D)(new ObjectInputStream(new ByteArrayInputStream(mapBytes)).readObject());
			paths.add(path);
		} catch(Exception e) {
			System.out.println("Some kind of exception getting map from user \"" + p.username + '\"');
			e.printStackTrace();
		}
	}

	private Path2D createDefaultPath() {
		Path2D p = new Path2D.Float();
		p.moveTo(4, 5);				// Top left
		p.lineTo(752, 5);				// Top right
		p.lineTo(752, 428);			// Bottom right
		p.lineTo(4, 428);				// Bottom left
		p.closePath();
		return p;
	}
}
