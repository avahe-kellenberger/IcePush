package com.glgames.graphics3d;

import java.awt.*;
import java.awt.image.MemoryImageSource;

import com.glgames.graphics2d.Renderer;

public class Renderer3D extends Renderer {

	// 3D camera stuff
	public double cameraX = 0.0;
	public double cameraY = -100.0;
	public double cameraZ = -450.0;
	public int pitch = 270, yaw = 180;
	public double focusX, focusY, focusZ;
	private double yawSin, yawCos, pitchSin, pitchCos;

	// 3D drawing stuff
	protected MemoryImageSource memsrc;
	protected Image memimg;
	protected int pixels[];
	private Face faceArray[];
	private int faceIndex;
	static int triLen;

	// 3D drawing constants
	public static final int HALF_GAME_FIELD_WIDTH = (744 / 2);
	public static final int HALF_GAME_FIELD_HEIGHT = (422 / 2);
	int scaledWidth;

	public Renderer3D(Component c, int w, int h) {
		super(c, w, h);
		scaledWidth = width << 12;
		faceArray = new Face[5000];
	}

	public void initGraphics() {
		super.initGraphics();
		pixels = new int[width * height];
		memsrc = new MemoryImageSource(width, height, pixels, 0, width);
		memsrc.setAnimated(true);
		memimg = canvas.createImage(memsrc);
	}

	protected void renderScene3D(Object3D objArray[], Object3D[] scenery) {
		clear();
		doRender(scenery);
		doRender(objArray);
		memsrc.newPixels();
		bg.drawImage(memimg, 0, 0, null);
		drawDebug();
	}

	private void doRender(Object3D[] objArray) {
		faceIndex = 0;
		while (pitch < 0)
			pitch += 360;
		while (pitch > 360)
			pitch -= 360;
		
		for (Object3D obj : objArray) {
			if (obj == null)
				continue;
			
			double yawRad = Math.toRadians(yaw);
			yawSin = Math.sin(yawRad);
			yawCos = Math.cos(yawRad);

			double pitchRad = Math.toRadians(pitch);
			pitchSin = Math.sin(pitchRad);
			pitchCos = Math.cos(pitchRad);

			int vertexCount;
			for (int currentFace = 0; currentFace < obj.faceVertices.length; currentFace++) {
				boolean withinViewport = false;
				if (obj.faceVertices[currentFace] == null)
					continue;

				vertexCount = obj.faceVertices[currentFace].length;

				double faceCenterX = 0;
				double faceCenterY = 0;
				double faceCenterZ = 0;

				// Will be discarded if this face is culled
				int drawXBuf[] = new int[vertexCount];
				int drawYBuf[] = new int[vertexCount];
				int drawZBuf[] = new int[vertexCount];

				for (int currentVertex = 0; currentVertex < vertexCount; currentVertex++) {
					int vertexID = obj.faceVertices[currentFace][currentVertex];

					double[] transformed = transformPoint(obj.baseX
							- HALF_GAME_FIELD_WIDTH, obj.baseY, obj.baseZ
							- HALF_GAME_FIELD_HEIGHT, obj.vertX[vertexID],
							obj.vertY[vertexID], obj.vertZ[vertexID]);

					obj.vertXRelCam[vertexID] = transformed[0];
					obj.vertYRelCam[vertexID] = transformed[1];
					obj.vertZRelCam[vertexID] = transformed[2];

					if (obj.vertZRelCam[vertexID] <= 0)
						obj.vertZRelCam[vertexID] = 1;

					int[] screen = worldToScreen(obj.vertXRelCam[vertexID],
							obj.vertYRelCam[vertexID],
							obj.vertZRelCam[vertexID]);

					obj.screenX[vertexID] = screen[0];
					obj.screenY[vertexID] = screen[1];

					faceCenterX += obj.vertXRelCam[vertexID];
					faceCenterY += obj.vertYRelCam[vertexID];
					faceCenterZ += obj.vertZRelCam[vertexID];

					int drawX = obj.screenX[vertexID];
					int drawY = obj.screenY[vertexID];
					int drawZ = (int) (1 / obj.vertZRelCam[vertexID]);

					if (drawX >= 0 && drawX <= width && drawY >= 0
							&& drawY <= height)
						withinViewport = true;

					drawXBuf[currentVertex] = drawX;
					drawYBuf[currentVertex] = drawY;
					drawZBuf[currentVertex] = drawZ;
				}

				if (!withinViewport)
					continue;

				faceCenterX /= vertexCount;
				faceCenterY /= vertexCount;
				faceCenterZ /= vertexCount;

				double distance = faceCenterX * faceCenterX + faceCenterY
						* faceCenterY + faceCenterZ * faceCenterZ;

				if (faceIndex > 4998)
					faceIndex = 4998;
				if (obj.faceColors != null) {
					faceArray[faceIndex++] = new Face(drawXBuf, drawYBuf,
							drawZBuf, vertexCount, distance,
							obj.faceColors[currentFace], null);
				} else if (obj.faceTextures != null) {
					faceArray[faceIndex++] = new Face(drawXBuf, drawYBuf,
							drawZBuf, vertexCount, distance, null,
							obj.faceTextures[currentFace]);
				}
			}
		}
		Triangle[] tris = triangulatePolygons(faceArray, faceIndex);
		java.util.Arrays.sort(tris, 0, triLen);

		for (int i = triLen - 1; i >= 0; i--) {
			Triangle t = tris[i];
			if(t.color != null)
				solidTriangle(t.x1, t.y1, t.x2, t.y2, t.x3, t.y3, t.color.getRGB());
		}
	}
	
	public void clear() {
		int i = width * height;
		for (int j = 0; j < i; j++)
			pixels[j] = 0;
	}

	private Triangle[] triangulatePolygons(Face[] faces, int len) {
		Triangle[] out = new Triangle[faceIndex * (6 - 2)];
		int num = 0;
		for (int k = 0; k < len; k++) {
			Face f = faces[k];
			int fanX = f.drawX[0];
			int fanY = f.drawY[0];

			// Skip the adjacent vertices
			for (int vertex = 2; vertex < f.drawX.length; vertex++) {
				Triangle t = new Triangle();
				t.x1 = fanX;
				t.y1 = fanY;
				t.x2 = f.drawX[vertex - 1];
				t.y2 = f.drawY[vertex - 1];
				t.x3 = f.drawX[vertex];
				t.y3 = f.drawY[vertex];
				
				t.distance = f.distance;
				t.color = f.color;
				out[num++] = t;
			}
		}
		triLen = num;
		return out;
	}

	public void drawDebug() {
		if (bg == null)
			return;
		bg.setColor(Color.white);
		bg.setFont(debugFont);
		bg.drawString("3D Renderer - Camera X: " + cameraX + ", Y: " + cameraY
				+ ", Z: " + cameraZ + ", Pitch: " + pitch + ", Yaw: " + yaw,
				15, 15);
	}

	public int[] worldToScreen(double x, double y, double z) {
		int[] ret = new int[2];
		int sW = width / 2, sH = height / 2;

		ret[0] = sW - (int) (sW * x / z); // Fix for bug #433299297: Left and
											// right are transposed
		ret[1] = sH - (int) (sH * y / z);
		return ret;
	}

	public double[] transformPoint(double objBaseX, double objBaseY,
			double objBaseZ, double vertX, double vertY, double vertZ) {
		double absVertX = objBaseX + vertX;
		double absVertY = objBaseY + vertY;
		double absVertZ = objBaseZ + vertZ;
		// System.out.println(absVertX + " " + absVertY + " " + absVertZ);
		absVertX -= focusX;
		absVertY -= focusY;
		absVertZ -= focusZ;

		/* Rotation about Y axis -- Camera Yaw */

		double rotated_Y_AbsVertX = (absVertX * yawCos - absVertZ * yawSin);
		double rotated_Y_AbsVertZ = (absVertX * yawSin + absVertZ * yawCos);

		/* Rotation about X axis -- Camera Pitch */

		double rotated_X_AbsVertY = (absVertY * pitchCos - rotated_Y_AbsVertZ
				* pitchSin);
		double rotated_X_AbsVertZ = (absVertY * pitchSin + rotated_Y_AbsVertZ
				* pitchCos);

		rotated_Y_AbsVertX += focusX;
		rotated_X_AbsVertY += focusY;
		rotated_X_AbsVertZ += focusZ;

		return new double[] { rotated_Y_AbsVertX - cameraX,
				rotated_X_AbsVertY - cameraY, rotated_X_AbsVertZ - cameraZ };
	}

	/**
	 * Sorts a trio of vertices by height so that y1 <= y2 <= y3 Implemented as
	 * its own method with global variables so that triangle code can stay small
	 * (Crucial to fitting within CPU code cache for these presumably highly
	 * performance intensive routines.)
	 */

	private static int _x1, _y1, _x2, _y2, _x3, _y3;

	private static void triSort(int x1, int y1, int x2, int y2, int x3, int y3) {
		_x1 = x1;
		_y1 = y1;
		_x2 = x2;
		_y2 = y2;
		_x3 = x3;
		_y3 = y3;
		int exchx = 0, exchy = 0; // two exchange variables are used to take
		// advantage of potential ILP

		if (_y1 > _y2) { // Each exchange block is dependant on the previous
			// one, so crap
			exchx = _x1;
			exchy = _y1;

			_x1 = _x2;
			_y1 = _y2;

			_x2 = exchx;
			_y2 = exchy;
		}

		if (_y1 > _y3) {
			exchx = _x1;
			exchy = _y1;

			_x1 = _x3;
			_y1 = _y3;

			_x3 = exchx;
			_y3 = exchy;
		}

		if (_y2 > _y3) {
			exchx = _x2;
			exchy = _y2;

			_x2 = _x3;
			_y2 = _y3;

			_x3 = exchx;
			_y3 = exchy;
		}
	}

	public void solidTriangle(int X1, int Y1, int X2, int Y2, int X3, 
			int Y3, int color) {
		triSort(X1, Y1, X2, Y2, X3, Y3);
		if (_y3 == _y1)
			return;

		int step31 = scaledWidth + ((_x3 - _x1) << 12) / (_y3 - _y1);

		int right = _y1 * scaledWidth, left = right;
		int boundLeft = _y1 * width, boundRight = boundLeft + width;

		if (_y1 != _y2) {
			right += _x1 << 12;
			left = right;
			int step21 = scaledWidth + ((_x2 - _x1) << 12) / (_y2 - _y1);
			// Top part, triangle is broadening. stepLeft < stepRight
			int stepLeft = 0, stepRight = 0;
			if (step21 > step31) {
				stepLeft = step31;
				stepRight = step21;
			} else {
				stepLeft = step21;
				stepRight = step31;
			}
			while (_y1 != _y2) {
				if (_y1 >= 0 && _y1 < height) {
					int l = left >> 12;
					int r = right >> 12;

					if (l < boundLeft)
						l = boundLeft;
					if (r > boundRight)
						r = boundRight;

					while (l < r)
						pixels[l++] = color;
				}
				left += stepLeft;
				right += stepRight;
				boundLeft = boundRight;
				boundRight += width;
				_y1++;
			}
		} else {
			// Triangle is flat topped; adjust left & right accordingly + skip
			// filling top half
			if (_x1 > _x2) {
				right += _x1 << 12;
				left += _x2 << 12;
			} else {
				right += _x2 << 12;
				left += _x1 << 12;
			}
		}

		if (_y2 != _y3) {
			int step23 = scaledWidth + ((_x2 - _x3) << 12) / (_y2 - _y3);
			// Bottom part: Triangle is narrowing. stepLeft > stepRight.
			int stepLeft = 0, stepRight = 0;
			if (step23 > step31) {
				stepLeft = step23;
				stepRight = step31;
			} else {
				stepLeft = step31;
				stepRight = step23;
			}
			while (_y2 != _y3) {
				if (_y2 >= 0 && _y2 < height) {
					int l = left >> 12;
					int r = right >> 12;

					if (l < boundLeft)
						l = boundLeft;
					if (r > boundRight)
						r = boundRight;

					while (l < r)
						pixels[l++] = color;
				}

				left += stepLeft;
				right += stepRight;
				boundLeft = boundRight;
				boundRight += width;
				_y2++;
			}
		}
	}
}
