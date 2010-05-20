package com.glgames.game;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;

public class Renderer3D extends Renderer {
	private static final long serialVersionUID = 1L;
	
	public Renderer3D() {
		super(new Canvas());
		faceArray = new Face[5000];
		cameraY = 100;
	}

	public void renderScene(Object3D objArray[]) {
		Object3D[] total = new Object3D[objArray.length
				+ GameObjects.scenery.length];
		System.arraycopy(objArray, 0, total, 0, objArray.length - 1);
		System.arraycopy(GameObjects.scenery, 0, total, objArray.length + 1,
				GameObjects.scenery.length - 1);
		doRender(total);
	}
	
	private void doRender(Object3D[] objArray) {
		double objBaseX, objBaseY, objBaseZ;

		faceIndex = 0;

		for (Object3D obj : objArray) {
			if (obj == null)
				continue;
			
			objBaseX = obj.baseX;
			objBaseY = obj.baseY;
			objBaseZ = obj.baseZ;

			double yawRad = Math.toRadians(((double) yaw) + obj.rotationY);
			yawSin = Math.sin(yawRad);
			yawCos = Math.cos(yawRad);

			double pitchRad = Math.toRadians(((double) pitch) + obj.rotationX);
			pitchSin = Math.sin(pitchRad);
			pitchCos = Math.cos(pitchRad);

			for (int i = 0; i < obj.vertexCount; i++) {
				double[] transformed = transformPoint(objBaseX, objBaseY,
						objBaseZ, obj.vertX[i], obj.vertY[i], obj.vertZ[i]);

				obj.vertXRelCam[i] = transformed[0];
				obj.vertYRelCam[i] = transformed[1];
				obj.vertZRelCam[i] = transformed[2];
				
				int[] screen = worldToScreen(obj.vertXRelCam[i],
						obj.vertYRelCam[i], obj.vertZRelCam[i]);
				
				obj.screenX[i] = screen[0];
				obj.screenY[i] = screen[1];
			}

			int vertexCount;

			// faceLoop:
			// Number of faces this object has
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

				for (int currentVertex = 0; currentVertex < vertexCount; currentVertex++) {
					int vertexID = obj.faceVertices[currentFace][currentVertex];

					faceCenterX += obj.vertXRelCam[vertexID];
					faceCenterY += obj.vertYRelCam[vertexID];
					faceCenterZ += obj.vertZRelCam[vertexID];

					if (obj.vertZRelCam[vertexID] <= 0)
						continue;
					
					int drawX = obj.screenX[vertexID];
					int drawY = obj.screenY[vertexID];
					
					if (drawX >= 0 && drawX <= GameFrame.WIDTH && drawY >= 0
							&& drawY <= GameFrame.HEIGHT)
						withinViewport = true;

					drawXBuf[currentVertex] = drawX;
					drawYBuf[currentVertex] = drawY;
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
				
				faceArray[faceIndex++] = new Face(drawXBuf, drawYBuf,
						vertexCount, distance, obj.faceColors[currentFace]);
			}
		}

		java.util.Arrays.sort(faceArray, 0, faceIndex);

		for (int i = faceIndex - 1; i >= 0; i--) {
			faceArray[i].draw(bg);
		}

	}

	public void drawDebug() {
		bg.setColor(Color.white);
		bg.setFont(new Font(Font.DIALOG, Font.PLAIN, 9));
		bg.drawString("3D Renderer - Camera X: " + cameraX + ", Y: " + cameraY + ", Z: "
				+ cameraZ + ", Pitch: " + pitch + ", Yaw: " + yaw, 15, 15);
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

	public int[] worldToScreen(double transX, double transY, double transZ) {
		int[] ret = new int[2];
		int sW = GameFrame.WIDTH / 2, sH = GameFrame.HEIGHT / 2;
		
		ret[0] = sW + (int) (sW * transX / transZ);
		ret[1] = sH - (int) (sH * transY / transZ);
		return ret;
	}

	public void focusCamera(int x, int z) {
		focusX = x;
		focusZ = z;

		cameraX = x;
		cameraZ = z - 200;
	}

	public double cameraX;
	public double cameraY;
	public double cameraZ;

	public int pitch, yaw;

	public double focusX, focusY, focusZ;

	private Face faceArray[];
	private int faceIndex;

	private double yawSin, yawCos, pitchSin, pitchCos;
}