package com.glgames.game;

import java.awt.Color;

public class Object3D extends GameObject {
	public double vertX[];
	public double vertY[];
	public double vertZ[];

	public int faceVertices[][];

	public Color faceColors[];

	public int screenX[];
	public int screenY[];

	public int vertexCount;

	public double baseX, baseY, baseZ;

	public double vertXRelCam[];
	public double vertYRelCam[];
	public double vertZRelCam[];
	
	public double clippedX[];
	public double clippedY[];
	public double clippedZ[];
	
	public double rotationY;
	public double rotationX;
	
	public static final double TWO_PI = 2 * Math.PI;
	public static Object3D templates[];

	public Object3D() {
		super(-1);
		vertexCount = 0;

		vertX = new double[50];
		vertY = new double[50];
		vertZ = new double[50];

		screenX = new int[50];
		screenY = new int[50];

		faceVertices = new int[50][];
		faceColors = new java.awt.Color[50];

		vertXRelCam = new double[50];
		vertYRelCam = new double[50];
		vertZRelCam = new double[50];
		
		clippedX = new double[50];
		clippedY = new double[50];
		clippedZ = new double[50];
	}

	public void putVertex(double x, double y, double z) {
		vertX[vertexCount] = x;
		vertY[vertexCount] = y;
		vertZ[vertexCount] = z;
		vertexCount++;
	}
	

	public Object3D(int vertices, int faces) {
		super(-1);
		vertexCount = vertices;

		vertX = new double[vertexCount];
		vertY = new double[vertexCount];
		vertZ = new double[vertexCount];

		screenX = new int[vertexCount];
		screenY = new int[vertexCount];

		faceVertices = new int[faces][];
		faceColors = new java.awt.Color[faces];

		vertXRelCam = new double[vertexCount];
		vertYRelCam = new double[vertexCount];
		vertZRelCam = new double[vertexCount];
		
		clippedX = new double[vertexCount];
		clippedY = new double[vertexCount];
		clippedZ = new double[vertexCount];
	}
	

	public Object3D(double layerHeights[], double layerScale[],
			Color layerColors[], Color top, Color bottom, int baseSides) {
		super(-1);
		double basePointX[] = new double[baseSides], basePointZ[] = new double[baseSides];

		for (int i = 0; i < baseSides; i++) {
			double ang = TWO_PI
					* (0.125F + ((double) i) / ((double) baseSides));
			basePointX[i] = Math.cos(ang);
			basePointZ[i] = Math.sin(ang);
		}

		int numLayers = layerHeights.length;
		faceColors = new Color[baseSides * numLayers + 2];

		faceVertices = new int[baseSides * (numLayers - 1)][];

		if (numLayers != layerScale.length
				|| numLayers != (1 + layerColors.length))
			throw new IllegalArgumentException();

		vertX = new double[2 + baseSides * numLayers];
		vertY = new double[2 + baseSides * numLayers];
		vertZ = new double[2 + baseSides * numLayers];

		vertexCount = 0;

		int faceCount = 0;
		double height = 0;

		for (int i = 0; i < numLayers; i++) {

			double scale = layerScale[i];
			height += layerHeights[i];

			for (int j = 0; j < baseSides; j++) {

				vertX[vertexCount] = 1.414 * scale * basePointX[j];
				vertY[vertexCount] = height;
				vertZ[vertexCount++] = 1.414 * scale * basePointZ[j];

				if (i != 0 && j != 0) {

					faceVertices[faceCount] = new int[] { vertexCount - 1,
							vertexCount - 2, vertexCount - (baseSides + 2),
							vertexCount - (baseSides + 1) };
					faceColors[faceCount++] = scaleColor(layerColors[i - 1],
							(5 - 2 * basePointX[j]) / 8);

				}

			}

			if (i == 0)
				continue;

			faceVertices[faceCount] = new int[] { vertexCount - 1,
					vertexCount - baseSides, vertexCount - (2 * baseSides),
					vertexCount - (baseSides + 1) };
			faceColors[faceCount++] = scaleColor(layerColors[i - 1],
					(5 - 2 * basePointX[0]) / 8);

		}

		vertXRelCam = new double[vertexCount];
		vertYRelCam = new double[vertexCount];
		vertZRelCam = new double[vertexCount];

		screenX = new int[vertexCount];
		screenY = new int[vertexCount];
		
		clippedX = new double[vertexCount];
		clippedY = new double[vertexCount];
		clippedZ = new double[vertexCount];
	}

	public Object3D(int type) {
		super(type);
		Object3D orig = templates[type];
		vertexCount = orig.vertexCount;

		vertX = orig.vertX.clone();
		vertY = orig.vertY.clone();
		vertZ = orig.vertZ.clone();

		screenX = new int[vertexCount];
		screenY = new int[vertexCount];

		faceVertices = orig.faceVertices.clone();
		faceColors = orig.faceColors.clone();

		vertXRelCam = new double[vertexCount];
		vertYRelCam = new double[vertexCount];
		vertZRelCam = new double[vertexCount];
		
		clippedX = new double[vertexCount];
		clippedY = new double[vertexCount];
		clippedZ = new double[vertexCount];
	}

	private static Color scaleColor(Color c, double scale) {
		int red = c.getRed();
		int green = c.getGreen();
		int blue = c.getBlue();

		red = (int) (scale * (double) red);
		green = (int) (scale * (double) green);
		blue = (int) (scale * (double) blue);

		if (red < 0)
			red = 0;
		if (red > 255)
			red = 255;

		if (green < 0)
			green = 0;
		if (green > 255)
			green = 255;

		if (blue < 0)
			blue = 0;
		if (blue > 255)
			blue = 255;

		return new Color(red, green, blue);
	}

	static {
		double smLayerHeights[] = new double[] { -20, 7, 10, 7, 5, 2.5, 2.5,
				2.5, 2.5 };
		double smLayerScale[] = new double[] { 5, 10, 10, 7, 6, 2.5, 4, 4, 2.5 };
		Color smLayerColors[] = new Color[8];
		smLayerColors[0] = Color.white;
		for (int i = 1; i < 8; i++)
			smLayerColors[i] = Color.white;

		double treeLayerHeights[] = new double[] { -20, 20, 0, 100 };
		double treeLayerScale[] = new double[] { 7, 10, 25, 0 };
		Color treeLayerColors[] = new Color[] { new Color(128, 128, 0),
				Color.green, Color.green };

		templates = new Object3D[4];

		templates[0] = new Object3D(treeLayerHeights, treeLayerScale,
				treeLayerColors, null, null, 6);
		templates[1] = new Object3D(smLayerHeights, smLayerScale,
				smLayerColors, null, null, 50);
	}
	
	public static class Plane extends Object3D {
		public Plane(int s) {
			putVertex(0, 0, 0);
			putVertex(s, 0, 0);
			putVertex(s, 0, s);
			putVertex(0, 0, s);
            faceVertices = new int[][] { { 0, 1, 2, 3 } };
            faceColors = new Color[] { new Color(200, 255, 255) };
		}
	}
	
	public static class Cube extends Object3D {
		public Cube(int s) {
			putVertex(0, 0, s); // left,top,front
			putVertex(s, 0, s); // right,top,front
			putVertex(s, 0, 0); // right,top,back
			putVertex(0, 0, 0); // left, top,back
			putVertex(0, -s, s); // left,bottom,front
			putVertex(s, -s, s); // right,bottom,front
			putVertex(s, -s, 0); // right,bottom,back
			putVertex(0, -s, 0); // left,bottom,back
			faceVertices = new int[][] { { 0, 1, 2, 3 }, // top
					{ 0, 3, 7, 4 }, // left
					{ 3, 2, 6, 7 }, // back
					{ 2, 1, 5, 6 }, // right
					{ 0, 4, 5, 1 }, // front
					{ 4, 7, 6, 5 } // bottom
			};
			faceColors = new Color[] {
				new Color(100, 0, 0),
				new Color(0, 100, 0),
				new Color(100, 0, 0),

				new Color(0, 100, 0),
				new Color(0, 0, 100),
				new Color(0, 0, 100)
			};
		}
	}
}