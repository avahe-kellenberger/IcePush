package net.threesided.graphics3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ObjImporter {
    static final String COMMENT = "#";
    static final int TEX_SIDE = 512; // True for all current icepush models

    // this is hackery but it works and makes sense to me
    public static Object3D loadObj(String name) throws IOException {
        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(ObjImporter.class.getResourceAsStream("/" + name)));
        Object3D obj = new Object3D(500, 500); // big enough for all models currently
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith(COMMENT) || line.isEmpty()) continue;
            line = line.replaceAll("( )+", " ");
            String[] parts = line.split(" ");
            String command = parts[0];
            if (command.equals("v")) {
                obj.putVertex(
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2]),
                        Double.parseDouble(parts[3]));
            } else if (command.equals("vt")) {
                int u = (int) ((TEX_SIDE * Double.parseDouble(parts[1])));
                int v = (int) (TEX_SIDE - (TEX_SIDE * Double.parseDouble(parts[2])));
                obj.putUV(u, v);
            } else if (command.equals("f")) {
                obj.beginFace(parts.length - 1); // -1 for 'f' part
                for (int idx = 1; idx < parts.length; idx++) {
                    String[] f = parts[idx].split("/");
                    obj.putFaceVert(
                            Integer.parseInt(f[0]) - 1,
                            f.length > 1 && !f[1].isEmpty() ? Integer.parseInt(f[1]) - 1 : 0);
                }
                obj.endFace(0);
            }
        }
        return obj;
    }
}
