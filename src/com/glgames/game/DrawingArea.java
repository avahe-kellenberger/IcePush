package com.glgames.game;

public class DrawingArea {

	public static void initDrawingArea(int i, int j, int ai[]) {
		pixels = ai;
		width = j;
		height = i;
		setDrawingArea(i, 0, j, 0);
	}

	public static void defaultDrawingAreaSize() {
		topX = 0;
		topY = 0;
		bottomX = width;
		bottomY = height;
		centerX = bottomX - 1;
		centerY = bottomX / 2;
	}

	public static void setDrawingArea(int i, int j, int k, int l) {
		if (j < 0)
			j = 0;
		if (l < 0)
			l = 0;
		if (k > width)
			k = width;
		if (i > height)
			i = height;
		topX = j;
		topY = l;
		bottomX = k;
		bottomY = i;
		centerX = bottomX - 1;
		centerY = bottomX / 2;
		anInt1387 = bottomY / 2;
	}

	public static void setAllPixelsToZero() {
		int i = width * height;
		for (int j = 0; j < i; j++)
			pixels[j] = 0;

	}

	public static void fillTransparentRect(int pixAdjust, int row, int numCols,
			int numRows, int pixScale, int col) {
		pixAdjust = 0xff00;
		if (col < topX) {
			numCols -= topX - col;
			col = topX;
		}
		if (row < topY) {
			numRows -= topY - row;
			row = topY;
		}
		if (col + numCols > bottomX)
			numCols = bottomX - col;
		if (row + numRows > bottomY)
			numRows = bottomY - row;
		int l1 = 256 - pixScale;
		int red = (pixAdjust >> 16 & 0xff) * pixScale;
		int green = (pixAdjust >> 8 & 0xff) * pixScale;
		int blue = (pixAdjust & 0xff) * pixScale;
		int lineLen = width - numCols;
		int pixidx = col + row * width;
		for (int i4 = 0; i4 < numRows; i4++) {
			for (int j4 = -numCols; j4 < 0; j4++) {
				int l2 = (pixels[pixidx] >> 16 & 0xff) * l1;
				int i3 = (pixels[pixidx] >> 8 & 0xff) * l1;
				int j3 = (pixels[pixidx] & 0xff) * l1;
				int newPixel = ((red + l2 >> 8) << 16)
						+ ((green + i3 >> 8) << 8) + (blue + j3 >> 8);
				pixels[pixidx++] = newPixel;
			}

			pixidx += lineLen;
		}
	}

	// fillRect width startY startX color height
	public static void fillRectangle(int i, int j, int k, int l, int i1) {
		if (k < topX) {
			i1 -= topX - k;
			k = topX;
		}
		if (j < topY) {
			i -= topY - j;
			j = topY;
		}
		if (k + i1 > bottomX)
			i1 = bottomX - k;
		if (j + i > bottomY)
			i = bottomY - j;
		int k1 = width - i1;
		int l1 = k + j * width;
		for (int i2 = -i; i2 < 0; i2++) {
			for (int j2 = -i1; j2 < 0; j2++)
				pixels[l1++] = l;

			l1 += k1;
		}

	}

	// drawRect
	public static void drawRectangle(int i, int j, int k, int l, int i1) {
		drawHorizontalLine(i1, l, j, i);
		drawHorizontalLine((i1 + k) - 1, l, j, i);
		drawVerticalLine(i1, l, k, i);
		drawVerticalLine(i1, l, k, (i + j) - 1);
	}

	// drawTransparentRect
	public static void method338(int i, int j, int k, int l, int i1, int j1) {
		method340(l, i1, i, k, j1);
		method340(l, i1, (i + j) - 1, k, j1);
		if (j >= 3) {
			method342(l, j1, k, i + 1, j - 2);
			method342(l, (j1 + i1) - 1, k, i + 1, j - 2);
		}
	}

	// drawHorizontalLine
	public static void drawHorizontalLine(int row, int j, int len, int col) {
		if (row < topY || row >= bottomY)
			return;
		if (col < topX) {
			len -= topX - col;
			col = topX;
		}
		if (col + len > bottomX)
			len = bottomX - col;
		int start = col + row * width;
		for (int j1 = 0; j1 < len; j1++)
			pixels[start + j1] = j;

	}

	// drawHorizontalLineAlpha
	private static void method340(int i, int j, int k, int l, int i1) {
		System.out.println("method340");
		int j1 = 256 - l;
		int k1 = (i >> 16 & 0xff) * l;
		int l1 = (i >> 8 & 0xff) * l;
		int i2 = (i & 0xff) * l;
		int i3 = i1 + k * width;
		System.out.println("Adjust components: Red: " + k1 + " green: " + l1
				+ " blue: " + i2);
		System.out.println("256 - scale: " + j1 + " pixelStart: " + i3);

		/*
		 * if (k < topY || k >= bottomY) return; if (i1 < topX) { j -= topX -
		 * i1; i1 = topX; } if (i1 + j > bottomX) j = bottomX - i1; int j1 = 256
		 * - l; int k1 = (i >> 16 & 0xff) * l; int l1 = (i >> 8 & 0xff) * l; int
		 * i2 = (i & 0xff) * l; int i3 = i1 + k * width; for (int j3 = 0; j3 <
		 * j; j3++) { int j2 = (pixels[i3] >> 16 & 0xff) * j1; int k2 =
		 * (pixels[i3] >> 8 & 0xff) * j1; int l2 = (pixels[i3] & 0xff) * j1; int
		 * k3 = ((k1 + j2 >> 8) << 16) + ((l1 + k2 >> 8) << 8) + (i2 + l2 >> 8);
		 * pixels[i3++] = k3; }
		 */

	}

	// drawVerticalLine method341
	public static void drawVerticalLine(int row, int j, int k, int col) {
		if (col < topX || col >= bottomX)
			return;
		if (row < topY) {
			k -= topY - row;
			row = topY;
		}
		if (row + k > bottomY)
			k = bottomY - row;
		int start = col + row * width;
		for (int k1 = 0; k1 < k; k1++)
			pixels[start + k1 * width] = j;

	}

	// drawVerticalLineAlpha
	private static void method342(int i, int j, int k, int l, int i1) {
		if (j < topX || j >= bottomX)
			return;
		if (l < topY) {
			i1 -= topY - l;
			l = topY;
		}
		if (l + i1 > bottomY)
			i1 = bottomY - l;
		int j1 = 256 - k;
		int k1 = (i >> 16 & 0xff) * k;
		int l1 = (i >> 8 & 0xff) * k;
		int i2 = (i & 0xff) * k;
		int i3 = j + l * width;
		for (int j3 = 0; j3 < i1; j3++) {
			int j2 = (pixels[i3] >> 16 & 0xff) * j1;
			int k2 = (pixels[i3] >> 8 & 0xff) * j1;
			int l2 = (pixels[i3] & 0xff) * j1;
			int k3 = ((k1 + j2 >> 8) << 16) + ((l1 + k2 >> 8) << 8)
					+ (i2 + l2 >> 8);
			pixels[i3] = k3;
			i3 += width;
		}

	}

	public DrawingArea() {
	}

	public static void copy(int[] src, int[] dest, int srcW, int srcH, int destW) {
		for (int x = 0; x < srcW; x++)
			for (int y = 0; y < srcH; y++)
				dest[y * destW + x] = src[y * srcW + x];
	}

	public static int pixels[];
	public static int width;
	public static int height;
	public static int topY;
	public static int bottomY;
	public static int topX;
	public static int bottomX;
	public static int centerX;
	public static int centerY;
	public static int anInt1387;
}
