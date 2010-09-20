package com.glgames.graphics3d;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;

import com.glgames.graphics2d.Renderer;

public class Renderer3D extends Renderer {

	// 3D camera stuff
	public double cameraX = 380.0;
	public double cameraY = 390.0;
	public double cameraZ = 330.0;
	public int pitch = 270, yaw = 0;
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
				Face out = new Face(vertexCount);
				double faceCenterX = 0;
				double faceCenterY = 0;
				double faceCenterZ = 0;
				
				for (int currentVertex = 0; currentVertex < vertexCount; currentVertex++) {
					int vertexID = obj.faceVertices[currentFace][currentVertex];
					
					double[] transformed = transformPoint(obj.baseX, obj.baseY, obj.baseZ, obj.vertX[vertexID],
							obj.vertY[vertexID], obj.vertZ[vertexID]);
					
					obj.vertXRelCam[vertexID] = transformed[0];
					obj.vertYRelCam[vertexID] = transformed[1];
					obj.vertZRelCam[vertexID] = transformed[2];
					
					if (obj.vertZRelCam[vertexID] <= 0)
						obj.vertZRelCam[vertexID] = 1;

					int[] screen = worldToScreen(obj.vertXRelCam[vertexID], obj.vertYRelCam[vertexID],
							obj.vertZRelCam[vertexID]);

					obj.screenX[vertexID] = screen[0];
					obj.screenY[vertexID] = screen[1];

					faceCenterX += obj.vertXRelCam[vertexID];
					faceCenterY += obj.vertYRelCam[vertexID];
					faceCenterZ += obj.vertZRelCam[vertexID];

					int drawX = obj.screenX[vertexID];
					int drawY = obj.screenY[vertexID];
					float drawZ = (float) obj.vertZRelCam[vertexID];

					if (drawX >= 0 && drawX <= width && drawY >= 0
							&& drawY <= height)
						withinViewport = true;
					
					out.x[currentVertex] = drawX;
					out.y[currentVertex] = drawY;
					out.z[currentVertex] = drawZ;
					if(obj.faceuv.length > 0 && obj.faceuv[0] != null) {
						out.color = -1;
						int uvID = obj.faceuv[currentFace][currentVertex];
						out.u[currentVertex] = obj.U[uvID];
						out.v[currentVertex] = obj.V[uvID];
					}
				}

				if (!withinViewport)
					continue;

				faceCenterX /= vertexCount;
				faceCenterY /= vertexCount;
				faceCenterZ /= vertexCount;

				double distance = faceCenterX * faceCenterX + faceCenterY
						* faceCenterY + faceCenterZ * faceCenterZ;
				out.distance = distance;
				if(obj.faceuv.length == 0 || obj.faceuv[0] == null)
					out.color = obj.faceColors[currentFace].getRGB();
				
				faceArray[faceIndex++] = out;
			}
		}
		Arrays.sort(faceArray, 0, faceIndex);
		for(int k = faceIndex - 1; k >= 0; k--) {
			Face f = faceArray[k];
			if(f.color == -1) {
				perspectiveCorrectPolygon(f.x, f.y, f.z, f.u, f.v, Object3D.textures[f.texID], 512);
			} else {
				fillPolygon(f.x, f.y, f.color);
			}
		}
	}
	
	public void clear() {
		int i = width * height;
		for (int j = 0; j < i; j++)
			// Set all pixels to black
			pixels[j] = 0xFF000000;
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

	final int hw = width / 2, hh = height / 2;
	public int[] worldToScreen(double x, double y, double z) {
		int[] ret = new int[2];
		ret[0] = hw + (int) (hw * x / z);
		ret[1] = hh + (int) (hh * y / z);
		return ret;
	}
	
	public double[] transformPoint(double objBaseX, double objBaseY,
			double objBaseZ, double x, double y, double z) {
		x -= cameraX;
		y -= cameraY;
		z -= cameraZ;
		
		x += objBaseX;
		y += objBaseY;
		z += objBaseZ;
		
		double transX, transY, transZ;
		transX = (x * yawCos - z * yawSin);
		transZ = (x * yawSin + z * yawCos);
		
		transY = (y * pitchCos - transZ * pitchSin);
		transZ = (y * pitchSin + transZ * pitchCos);
		
		return new double[] { transX, transY, transZ };
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
	
	static float dizdx, duizdx, dvizdx, dizdy, duizdy, dvizdy;
	static float xa, xb, iza, uiza, viza;
	static float dxdya, dxdyb, dizdya, duizdya, dvizdya;
	
	/**
	 * Fills a textured triangle which is perspective-corrected at each
	 * pixel. Original implementation at: 
	 * http://www.lysator.liu.se/~mikaelk/doc/perspectivetexture/
	 * Implemented in Java by Tekk.
	 */
	public void perspectiveCorrectTriangle(float x1, float y1, float z1,
			float x2, float y2, float z2, float x3, float y3, float z3, int u1,
			int v1, int u2, int v2, int u3, int v3, int[] tex, int sidelen) {
		float iz1, uiz1, viz1, iz2, uiz2, viz2, iz3, uiz3, viz3;
		float dxdy1 = 0, dxdy2 = 0, dxdy3 = 0;
		float tempf;
		float det;
		float dy;
		int y1i, y2i, y3i;
		boolean side;

		x1 += 0.5f; y1 += 0.5f;
		x2 += 0.5f; y2 += 0.5f;
		x3 += 0.5f; y3 += 0.5f;
		
		iz1 = 1.0f / z1;
		iz2 = 1.0f / z2;
		iz3 = 1.0f / z3;
		uiz1 = u1 * iz1;
		viz1 = v1 * iz1;
		uiz2 = u2 * iz2;
		viz2 = v2 * iz2;
		uiz3 = u3 * iz3;
		viz3 = v3 * iz3;

		if (y1 > y2) {
			tempf = x1; x1 = x2; x2 = tempf;
			tempf = y1; y1 = y2; y2 = tempf;
			tempf = iz1; iz1 = iz2; iz2 = tempf;
			tempf = uiz1; uiz1 = uiz2; uiz2 = tempf;
			tempf = viz1; viz1 = viz2; viz2 = tempf;

		}
		if (y1 > y3) {
			tempf = x1; x1 = x3; x3 = tempf;
			tempf = y1; y1 = y3; y3 = tempf;
			tempf = iz1; iz1 = iz3; iz3 = tempf;
			tempf = uiz1; uiz1 = uiz3; uiz3 = tempf;
			tempf = viz1; viz1 = viz3; viz3 = tempf;
		}
		if (y2 > y3) {
			tempf = x2; x2 = x3; x3 = tempf;
			tempf = y2; y2 = y3; y3 = tempf;
			tempf = iz2; iz2 = iz3; iz3 = tempf;
			tempf = uiz2; uiz2 = uiz3; uiz3 = tempf;
			tempf = viz2; viz2 = viz3; viz3 = tempf;
		}

		y1i = (int) y1;
		y2i = (int) y2;
		y3i = (int) y3;

		if (y1 == y3)
			return;

		det = ((x3 - x1) * (y2 - y1) - (x2 - x1) * (y3 - y1));

		if (det == 0)
			return;

		det = 1 / det;
		dizdx = ((iz3 - iz1) * (y2 - y1) - (iz2 - iz1) * (y3 - y1)) * det;
		duizdx = ((uiz3 - uiz1) * (y2 - y1) - (uiz2 - uiz1) * (y3 - y1)) * det;
		dvizdx = ((viz3 - viz1) * (y2 - y1) - (viz2 - viz1) * (y3 - y1)) * det;
		dizdy = ((iz2 - iz1) * (x3 - x1) - (iz3 - iz1) * (x2 - x1)) * det;
		duizdy = ((uiz2 - uiz1) * (x3 - x1) - (uiz3 - uiz1) * (x2 - x1)) * det;
		dvizdy = ((viz2 - viz1) * (x3 - x1) - (viz3 - viz1) * (x2 - x1)) * det;

		if (y2 > y1)
			dxdy1 = (x2 - x1) / (y2 - y1);
		if (y3 > y1)
			dxdy2 = (x3 - x1) / (y3 - y1);
		if (y3 > y2)
			dxdy3 = (x3 - x2) / (y3 - y2);

		side = dxdy2 > dxdy1;

		if (y1 == y2)
			side = x1 > x2;
		if (y2 == y3)
			side = x3 > x2;

		if (!side) {
			dxdya = dxdy2;
			dizdya = dxdy2 * dizdx + dizdy;
			duizdya = dxdy2 * duizdx + duizdy;
			dvizdya = dxdy2 * dvizdx + dvizdy;

			dy = 1 - (y1 - y1i);
			xa = x1 + dy * dxdya;
			iza = iz1 + dy * dizdya;
			uiza = uiz1 + dy * duizdya;
			viza = viz1 + dy * dvizdya;

			if (y1i < y2i) {
				xb = x1 + dy * dxdy1;
				dxdyb = dxdy1;

				perspectiveCorrectSegment(y1i, y2i, tex, sidelen);
			}
			if (y2i < y3i) {
				xb = x2 + (1 - (y2 - y2i)) * dxdy3;
				dxdyb = dxdy3;

				perspectiveCorrectSegment(y2i, y3i, tex, sidelen);
			}
		} else {
			dxdyb = dxdy2;
			dy = 1 - (y1 - y1i);
			xb = x1 + dy * dxdyb;

			if (y1i < y2i) {
				dxdya = dxdy1;
				dizdya = dxdy1 * dizdx + dizdy;
				duizdya = dxdy1 * duizdx + duizdy;
				dvizdya = dxdy1 * dvizdx + dvizdy;
				xa = x1 + dy * dxdya;
				iza = iz1 + dy * dizdya;
				uiza = uiz1 + dy * duizdya;
				viza = viz1 + dy * dvizdya;

				perspectiveCorrectSegment(y1i, y2i, tex, sidelen);
			}
			if (y2i < y3i) {
				dxdya = dxdy3;
				dizdya = dxdy3 * dizdx + dizdy;
				duizdya = dxdy3 * duizdx + duizdy;
				dvizdya = dxdy3 * dvizdx + dvizdy;
				dy = 1 - (y2 - y2i);
				xa = x2 + dy * dxdya;
				iza = iz2 + dy * dizdya;
				uiza = uiz2 + dy * duizdya;
				viza = viz2 + dy * dvizdya;

				perspectiveCorrectSegment(y2i, y3i, tex, sidelen);
			}
		}
	}

	/**
	 * Utility method called by perspectiveCorrectTriangle.
	 */
	private void perspectiveCorrectSegment(int y1, int y2, int[] tex, int sidelen) {
		int scr, idx;
		int x1, x2;
		float z, u, v, dx;
		float iz, uiz, viz;

		while (y1 < y2) {
			x1 = (int) xa;
			x2 = (int) xb;
			if(x1 < 0)
				x1 = 0;
			if (x2 > width - 1)
				x2 = width - 1;
			
			dx = 1 - (xa - x1);
			iz = iza + dx * dizdx;
			uiz = uiza + dx * duizdx;
			viz = viza + dx * dvizdx;
			scr = y1 * width + x1;
			
			while (x1++ < x2) {
				if(scr < 0 || scr > pixels.length - 1)
					continue;
				z = 1 / iz;
				u = uiz * z;
				v = viz * z;
				
				idx = ((int) v & (sidelen - 1)) * sidelen + ((int) u & (sidelen - 1));
				pixels[scr++] = tex[idx];
				iz += dizdx;
				uiz += duizdx;
				viz += dvizdx;
			}
			xa += dxdya;
			xb += dxdyb;
			iza += dizdya;
			uiza += duizdya;
			viza += dvizdya;
			
			y1++;
		}
	}
	
	
	/**
	 * Triangulates and then fills any convex polygon.
	 */
	public void fillPolygon(int[] x, int[] y, int color) {
		for (int k = 2; k < x.length; k++) {
			solidTriangle(x[0], y[0], x[k - 1], y[k - 1], x[k], y[k], color);
		}
	}
	
	/**
	 * A simple 'fan'-based method which triangulates and then textures any 
	 * convex polygon.
	 */
	public void perspectiveCorrectPolygon(int[] x, int[] y, float[] z, int[] u, int[] v,
			int[] texture, int sidelen) {
		for (int k = 2; k < x.length; k++) {
			perspectiveCorrectTriangle(x[0], y[0], z[0], x[k - 1], y[k - 1],
					z[k - 1], x[k], y[k], z[k], u[0], v[0], u[k - 1], v[k - 1],
					u[k], v[k], texture, sidelen);
		}
	}
}
