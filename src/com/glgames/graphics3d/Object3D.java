package com.glgames.graphics3d;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Object3D {
	public double vertX[];
	public double vertY[];
	public double vertZ[];

	public int faceVertices[][];
	public int faceCount;
	public Color faceColors[];

	public int screenX[];
	public int screenY[];
	public int vertexCount;

	public double baseX, baseY, baseZ;

	public double vertXRelCam[];
	public double vertYRelCam[];
	public double vertZRelCam[];
	
	public int[] U;
	public int[] V;
	public int numUV;
	
	public int[][] faceuv;
	public int[] facetextid;
	
	public int rotationY;
	public int rotationX;
	
	public static final double TWO_PI = 2 * Math.PI;
	public static Object3D templates[];
	public static int[][] textures;

	public Object3D() {
		vertexCount = 0;

		vertX = new double[50];
		vertY = new double[50];
		vertZ = new double[50];

		screenX = new int[50];
		screenY = new int[50];

		faceVertices = new int[50][];
		faceuv = new int[50][];
		facetextid = new int[50];
		faceColors = new java.awt.Color[50];
		
		U = new int[50];
		V = new int[50];

		vertXRelCam = new double[50];
		vertYRelCam = new double[50];
		vertZRelCam = new double[50];
	}

	public void putVertex(double x, double y, double z) {
		vertX[vertexCount] = x;
		vertY[vertexCount] = y;
		vertZ[vertexCount] = z;
		vertexCount++;
	}
	
	public void putUV(int u, int v) {
		U[numUV] = u;
		V[numUV++] = v;
	}
	
	private int[] tempvert, tempuv;
	private int tempptr;
	public void beginFace(int numPoints) {
		tempvert = new int[numPoints];
		tempuv = new int[numPoints];
		tempptr = 0;
	}
	
	public void putFaceVert(int pindex, int tindex) {
		tempvert[tempptr] = pindex;
		tempuv[tempptr++] = tindex;
	}
	
	public void endFace(int texid) {
		faceVertices[faceCount] = tempvert;
		faceuv[faceCount] = tempuv;
		facetextid[faceCount++] = texid;
	}
	
	public void scale(float scaleFactor) {
		for(int k = 0; k < vertX.length; k++) {
			vertX[k] *= scaleFactor;
			vertY[k] *= scaleFactor;
			vertZ[k] *= scaleFactor;
		}
	}

	public Object3D(int vertices, int faces) {
		vertX = new double[vertices];
		vertY = new double[vertices];
		vertZ = new double[vertices];
		vertXRelCam = new double[vertices];
		vertYRelCam = new double[vertices];
		vertZRelCam = new double[vertices];
		
		screenX = new int[vertices];
		screenY = new int[vertices];

		faceVertices = new int[faces][];
		faceColors = new Color[faces];
		faceuv = new int[faces][];
		facetextid = new int[faces];
		U = new int[vertices];
		V = new int[vertices];
	}
	

	public Object3D(double layerHeights[], double layerScale[],
			Color layerColors[], Color top, Color bottom, int baseSides) {
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
		faceuv = new int[0][];
		facetextid = new int[0];
		U = new int[0]; V = new int[0];
	}

	public Object3D(int type) {
		Object3D orig = templates[type];
		vertexCount = orig.vertexCount;

		vertX = orig.vertX.clone();
		vertY = orig.vertY.clone();
		vertZ = orig.vertZ.clone();

		screenX = new int[vertexCount];
		screenY = new int[vertexCount];

		faceVertices = orig.faceVertices.clone();
		faceColors = orig.faceColors.clone();
		faceuv = orig.faceuv.clone();
		facetextid = orig.facetextid.clone();
		
		U = orig.U.clone();
		V = orig.V.clone();

		vertXRelCam = new double[vertexCount];
		vertYRelCam = new double[vertexCount];
		vertZRelCam = new double[vertexCount];
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
		try {
			templates[1] = ObjImporter.loadObj("models/snowman.obj");
			templates[1].scale(0.3f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		textures = new int[2][];
		for(int k = 0; k < textures.length; k++)
			loadTexture(k, "models/" + k + ".jpg");
		
	}
	public static void loadTexture(int index, String filename) {
		try {
			BufferedImage img = ImageIO.read(Object3D.class.getResource("/" + filename));
			int w = img.getWidth();
			int h = img.getHeight();
			int size = w * h;
			int[] pix = new int[size];
			img.getRGB(0, 0, w, h, pix, 0, w);
			
			textures[index] = pix;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static class Plane extends Object3D {
		public Plane(double x, double y, double z, int verticesX, int verticesZ, double tileSize) {
			super(verticesX * verticesZ, (verticesX - 1) * (verticesZ - 1));

			//System.out.println("X = " + verticesX + " Z = " + verticesZ + " length: " + vertX.length);

			double vertexX = x, vertexZ = z;
			int currVertex = 0;

			int facesX = verticesX - 1, facesZ = verticesZ - 1;

			for(int vz = 0; vz < verticesZ; vz++) {
				for(int vx = 0; vx < verticesX; vx++) {		
					vertX[currVertex] = vertexX;
					vertY[currVertex] = y;
					vertZ[currVertex++] = vertexZ;
					vertexX += tileSize;
				}
				vertexX = x;
				vertexZ += tileSize;
			}

			int currFace = 0;
			int faceVert = 0;

			for(int fz = 0; fz < facesZ; fz++) {
				for(int fx = 0; fx < facesX; fx++) {
					faceColors[currFace] = new Color(100 + fz * 4, 100 + fx * 4, 196);
					faceVertices[currFace++] = new int[] { faceVert, faceVert + 1, faceVert + verticesX + 1, faceVert + verticesX};
					faceVert++;
				}
				faceVert++;
			}
		}
	}
}