package com.glgames.game;

import java.awt.Component;
import java.awt.Color;
import java.awt.Font;

public class Renderer3D extends Renderer {
	private static final long serialVersionUID = 1L;
	
	public Renderer3D(Component c) {
		super(c);
		faceArray = new Face[5000];
		cameraY = 100;
	}

	public void renderScene(Object3D objArray[], Object3D[] scenery) {
		Object3D[] total = new Object3D[objArray.length + scenery.length];
		System.arraycopy(objArray, 0, total, 0, objArray.length - 1);
		System.arraycopy(scenery, 0, total, objArray.length + 1,
				scenery.length - 1);
		doRender(total);
	}
	
	private void doRender(Object3D[] objArray) {
		faceIndex = 0;
		while(pitch < 0) pitch += 360;
		while(pitch > 360) pitch -= 360;
		
		for (Object3D obj : objArray) {
			if (obj == null)
				continue;
			double yawRad = Math.toRadians(((double) yaw) - obj.rotationY);
			yawSin = Math.sin(yawRad);
			yawCos = Math.cos(yawRad);

			double pitchRad = Math.toRadians(((double) pitch) - obj.rotationX);
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

				for (int currentVertex = 0; currentVertex < vertexCount; currentVertex++) {
					int vertexID = obj.faceVertices[currentFace][currentVertex];

					double[] transformed = transformPoint(obj.baseX, obj.baseY,
							obj.baseZ, obj.vertX[vertexID], obj.vertY[vertexID], obj.vertZ[vertexID]);

					obj.vertXRelCam[vertexID] = transformed[0];
					obj.vertYRelCam[vertexID] = transformed[1];
					obj.vertZRelCam[vertexID] = transformed[2];
					
					if(obj.vertZRelCam[vertexID] <= 0)
						obj.vertZRelCam[vertexID] = 1;
					
					int[] screen = worldToScreen(obj.vertXRelCam[vertexID],
							obj.vertYRelCam[vertexID], obj.vertZRelCam[vertexID]);
					
					obj.screenX[vertexID] = screen[0];
					obj.screenY[vertexID] = screen[1];
					
					faceCenterX += obj.vertXRelCam[vertexID];
					faceCenterY += obj.vertYRelCam[vertexID];
					faceCenterZ += obj.vertZRelCam[vertexID];
					
					if (obj.vertZRelCam[vertexID] <= 0);
					
					int drawX = obj.screenX[vertexID];
					int drawY = obj.screenY[vertexID];
					
					if (drawX >= 0 && drawX <= IcePush.WIDTH && drawY >= 0
							&& drawY <= IcePush.HEIGHT)
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
						vertexCount, distance, obj.faceColors[currentFace],
						null);
			}
		}

		java.util.Arrays.sort(faceArray, 0, faceIndex);

		for (int i = faceIndex - 1; i >= 0; i--) {
			faceArray[i].draw(bg);
		}

	}

	public void drawDebug() {
		if(bg == null)
			return;
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

	public int[] worldToScreen(double x, double y, double z) {
		int[] ret = new int[2];
		int sW = IcePush.WIDTH / 2, sH = IcePush.HEIGHT / 2;
		
		ret[0] = sW - (int) (sW * x / z);	// Fix for bug #433299297: Left and right are transposed
		ret[1] = sH - (int) (sH * y / z);
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

	public int pitch = 325, yaw;

	public double focusX, focusY, focusZ;

	private Face faceArray[];
	private int faceIndex;

	private double yawSin, yawCos, pitchSin, pitchCos;
}