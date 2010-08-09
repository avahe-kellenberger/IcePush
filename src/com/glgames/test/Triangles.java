package com.glgames.test;

import java.awt.Color;
import java.awt.Component;

public class Triangles {
	//static Pixmap pm;
	
	static int pixels[];
	static int width, height;
	static int lineOffs[];
	
	static int boundx;
	static int boundy;

	public static void init(int w, int h, Component component) {
		//pm = new Pixmap(w, h, component);
		//pixels = DrawingArea.pixels;
	//width = DrawingArea.width;
		//height = DrawingArea.height;
		boundx = width - 1;
		boundy = height - 1;
		scaledWidth = width << 12;
		lineOffs = new int[height];
		for (int i = 0; i < height; i++)
			lineOffs[i] = i * width;
	}

	public static void setDimensions(int x, int y) {
		//pixels = DrawingArea.pixels;
		width = x;
		height = y;
		boundx = x - 1;
		boundy = y - 1;
		scaledWidth = x << 12;
		lineOffs = new int[y];
		for (int i = 0; i < y; i++)
			lineOffs[i] = i * width;
	}

	public static void setAllPixelsToZero() {
		//DrawingArea.width = width;
		//DrawingArea.height = height;
		//DrawingArea.pixels = pixels;
		//DrawingArea.setAllPixelsToZero();
	}

	/**
	 * Contains derivation for the linear system solver.
	 */
	static void testMath(int x1, int y1, int x2, int y2, int x3, int y3,
			int z1, int z2, int z3) {

		float det = (x1 - x3) * (y1 - y2) - (x1 - x2) * (y1 - y3);

		if (det == 0) {
			System.out.println("Singularity\n");
			return;
		}

		float x = (y1 - y2) * (z1 - z3) - (y1 - y3) * (z1 - z2), y = (x1 - x3)
				* (z1 - z2) - (x1 - x2) * (z1 - z3), z = 0;

		x /= det;
		y /= det;
		z = z1 - (x1 * x + y1 * y);

		System.out.println("x=" + x + " y=" + y + " z=" + z);
		System.out.println("z1 = " + z1 + " :: " + (x1 * x + y1 * y + z));
		System.out.println("z2 = " + z2 + " :: " + (x2 * x + y2 * y + z));
		System.out.println("z3 = " + z3 + " :: " + (x3 * x + y3 * y + z));
		System.out.println();

		/*
		 * 
		 * A) x*x1 + y*y1 + z = z1 B) x*x2 + y*y2 + z = z2 C) x*x3 + y*y3 + z =
		 * z3
		 * 
		 * A - B:
		 * 
		 * x*(x1 - x2) + y*(y1 - y2) = (z1 - z2)
		 * 
		 * D) y = [(z1 - z2) - x*(x1 - x2)] / (y1 - y2)
		 * 
		 * A - C:
		 * 
		 * E) x*(x1 - x3) + y*(y1 - y3) = (z1 - z3)
		 * 
		 * sub D into E:
		 * 
		 * 
		 * x*(x1 - x3) + (y1 - y3)*[(z1 - z2) - x*(x1 - x2)] / (y1 - y2) = (z1 -
		 * z3)
		 * 
		 * x*(x1 - x3)*(y1 - y2) + (y1 - y3)*(z1 - z2) - x*(x1 - x2)*(y1 - y3) =
		 * (y1 - y2)*(z1 - z3)
		 * 
		 * F) x = [(y1 - y2)*(z1 - z3) - (y1 - y3)*(z1 - z2)] / [(x1 - x3)*(y1 -
		 * y2) - (x1 - x2)*(y1 - y3)]
		 * 
		 * sub F into D:
		 * 
		 * y*(y1 - y2) = (z1 - z2) - (x1 - x2)[(y1 - y2)*(z1 - z3) - (y1 -
		 * y3)*(z1 - z2)] / [(x1 - x3)*(y1 - y2) - (x1 - x2)*(y1 - y3)]
		 * 
		 * expand numerator:
		 * 
		 * y*(y1 - y2) = [
		 * 
		 * (x1 - x3)*(y1 - y2)*(z1 - z2) - (x1 - x2)*(y1 - y3)*(z1 - z2) - (x1 -
		 * x2)*(y1 - y2)*(z1 - z3) + (x1 - x2)(y1 - y3)*(z1 - z2)
		 * 
		 * ] / [(x1 - x3)*(y1 - y2) - (x1 - x2)*(y1 - y3)]
		 * 
		 * = [(x1 - x3)*(y1 - y2)*(z1 - z2) - (x1 - x2)*(y1 - y2)*(z1 - z3)] /
		 * [(x1 - x3)*(y1 - y2) - (x1 - x2)*(y1 - y3)]
		 * 
		 * cancel (y - y2) from both sides:
		 * 
		 * y = [(x1 - x3)*(z1 - z2) - (x1 - x2)*(z1 - z3)] / [(x1 - x3)*(y1 -
		 * y2) - (x1 - x2)*(y1 - y3)]
		 */

	}

	public static void shadedTriangle(int x1, int y1, int x2, int y2, int x3,
			int y3, int color, int b1, int b2, int b3) {
		fillGradientTriangle(x1, y1, x2, y2, x3, y3, brightenColor(color, b1),
				brightenColor(color, b2), brightenColor(color, b3));
	}

	public static int colorToInt(Color col) {
		return col.getRGB();
	}

	public static int brightenColor(int color, int brightness) {
		color |= 0x10101;

		int r = (color & 0xff0000) >> 16;
		int g = (color & 0xff00) >> 8;
		int b = color & 0xff;

		float bright = brightness / 255.0f;

		r = (int) Math.min(r * bright, 255);
		g = (int) Math.min(g * bright, 255);
		b = (int) Math.min(b * bright, 255);

		return (r << 16) | (g << 8) | b;
	}

	public static void setBrightness(int x, int y, int brightness) {
		pixels[width * y + x] = brightenColor(pixels[width * y + x], brightness);
	}

	private static int clippedX[] = new int[120];
	private static int clippedY[] = new int[120];

	private static int clippedVertexCount = 0;

	public static void clipHorz(int prevX, int prevY, int currX, int currY,
			int clipY, boolean toTop) {

		boolean prevIn = toTop ^ (prevY >= clipY); // Pixel is only above
		// boundary if strictly less
		boolean currIn = toTop ^ (currY >= clipY);

		if (toTop)
			clipY--; // So that calls down the line to emitVertex(.. clipY) are
		// actually in bounds

		if (prevIn && currIn) { // Both inside. Emit current.
			emitVertex(currX, currY);
		} else if ((!prevIn) && currIn) { // Prev out, curr in. Emit intserect
			// then current.
			emitVertex(prevX + ((clipY - prevY) * (prevX - currX))
					/ (prevY - currY), clipY);
			emitVertex(currX, currY);
		} else if (prevIn && (!currIn)) { // Curr out, prev in. Emit intersect.
			emitVertex(prevX + ((clipY - prevY) * (prevX - currX))
					/ (prevY - currY), clipY);
		}
	}

	public static void clipVert(int prevX, int prevY, int currX, int currY,
			int clipX, boolean toLeft) {

		boolean prevIn = toLeft ^ (prevX >= clipX); // Pixel is to the left of
		// the boundary only if
		// strictly less
		boolean currIn = toLeft ^ (currX >= clipX);

		if (toLeft)
			clipX--; // Ibid.

		if (prevIn && currIn) { // Both inside. Emit current.
			emitVertex(currX, currY);
		} else if ((!prevIn) && currIn) { // Prev out, curr in. Emit intserect
			// then current.
			emitVertex(clipX, prevY + ((clipX - prevX) * (prevY - currY))
					/ (prevX - currX));
			emitVertex(currX, currY);
		} else if (prevIn && (!currIn)) { // Curr out, prev in. Emit intersect.
			emitVertex(clipX, prevY + ((clipX - prevX) * (prevY - currY))
					/ (prevX - currX));
		}
	}

	private static int clipXBuf[] = new int[120];
	private static int clipYBuf[] = new int[120];
	private static int clipBufCount = 0;

	private static boolean emitToBuf = false;

	private static void emitVertex(int x, int y) {
		if (emitToBuf) {
			clipXBuf[clipBufCount] = x;
			clipYBuf[clipBufCount++] = y;
		} else {
			clippedX[clippedVertexCount] = x;
			clippedY[clippedVertexCount++] = y;
		}
	}

	/**
	 * Applies a gradient to a rectangle based on color given at three vertices.
	 * Used to test gradient code for being simpler than triangles.
	 */
	public static void testGradientRect(int x1, int y1, int x2, int y2, int x3,
			int y3, int color1, int color2, int color3) {
		int dy2_1 = y2 - y1;
		int dy3_1 = y3 - y1;

		int dx2_1 = x2 - x1;
		int dx3_1 = x3 - x1;

		int red1 = (color1 >> 16) & 0xff, green1 = (color1 >> 8) & 0xff, blue1 = (color1 & 0xff);
		int red2 = (color2 >> 16) & 0xff, green2 = (color2 >> 8) & 0xff, blue2 = (color2 & 0xff);
		int red3 = (color3 >> 16) & 0xff, green3 = (color3 >> 8) & 0xff, blue3 = (color3 & 0xff);

		float det = (dx3_1) * (dy2_1) - (dx2_1) * (dy3_1);

		if (det == 0) {
			System.out.println("Singularity\n");
			return;
		}

		float xred = (dy2_1) * (red3 - red1) - (dy3_1) * (red2 - red1), yred = (dx3_1)
				* (red2 - red1) - (dx2_1) * (red3 - red1);
		float xgreen = (dy2_1) * (green3 - green1) - (dy3_1)
				* (green2 - green1), ygreen = (dx3_1) * (green2 - green1)
				- (dx2_1) * (green3 - green1);
		float xblue = (dy2_1) * (blue3 - blue1) - (dy3_1) * (blue2 - blue1), yblue = (dx3_1)
				* (blue2 - blue1) - (dx2_1) * (blue3 - blue1);

		xred /= det;
		yred /= det;

		xgreen /= det;
		ygreen /= det;

		xblue /= det;
		yblue /= det;

		float zred = red1 - (x1 * xred + y1 * yred);
		float zgreen = green1 - (x1 * xgreen + y1 * ygreen);
		float zblue = blue1 - (x1 * xblue + y1 * yblue);

		int minx = x1, maxx = x1, miny = y1, maxy = y1;

		if (x2 < minx)
			minx = x2;
		if (x3 < minx)
			minx = x3;

		if (x2 > maxx)
			maxx = x2;
		if (x3 > maxx)
			maxx = x3;

		if (y2 < miny)
			miny = y2;
		if (y3 < miny)
			miny = y3;

		if (y2 > maxy)
			maxy = y2;
		if (y3 > maxy)
			maxy = y3;

		for (int i = minx; i < maxx; i++)
			for (int j = miny; j < maxy; j++)
				pixels[j * width + i] = (((int) (i * xred + j * yred + zred)) << 16)
						+ (((int) (i * xgreen + j * ygreen + zgreen)) << 8)
						+ ((int) (i * xblue + j * yblue + zblue));

		pixels[width * y1 + x1] = pixels[width * y2 + x2] = pixels[width * y3
				+ x3] = 0xffffff;
	}

	/**
	 * Generates a 64x64 texture gradiating from blue to green. Used to test
	 * perspective mapping.
	 */
	static int[] genTexture() {
		int[] texture = new int[4096];

		for (int i = 0; i < 64; i++)
			for (int j = 0; j < 64; j++) {
				texture[i + j * 64] =

				// (i << 17) + (j << 9) + 0xf;
				((i / 8) + (j / 8)) % 2 == 0 ? 0x99 : 0xffffff; //
				// Comment out the line about and uncomment this one to generate
				// red/white checkerboard pattern

				if (i == j)
					texture[i + j * 64] = 0xff; // Creates blue line of pixels
				// down diagonal to help
				// visually verify correct
				// perspective mapping
			}
		return texture;
	}

	/*
	 * int scanoff = top*width;
	 * 
	 * for(int j = top; j < bottom; j++) {
	 * 
	 * int pixel = a*left + b*j + c;
	 * 
	 * for(int i = left; i < right; i++) { pixels[scanoff + left] = pixel; pixel
	 * += a; }
	 * 
	 * scanoff += width; }
	 */


	/**
	 * Implementation of the above. Maps a triangle. A rectangular viewport is
	 * used for testing simplicity.
	 */
	public static void texturemapRectTest(int texture[], int texwidth, int u1,
			int v1, int u2, int v2, int u3, int v3, int x1, int y1, int x2,
			int y2, int x3, int y3, int z1, int z2, int z3) {

		int minx = x1, maxx = x1, miny = y1, maxy = y1;

		if (x2 < minx)
			minx = x2;
		if (x3 < minx)
			minx = x3;

		if (x2 > maxx)
			maxx = x2;
		if (x3 > maxx)
			maxx = x3;

		if (y2 < miny)
			miny = y2;
		if (y3 < miny)
			miny = y3;

		if (y2 > maxy)
			maxy = y2;
		if (y3 > maxy)
			maxy = y3;

		// #1: Map world x, y coords to u, v so that
		// 
		// ua.worldx + ub.worldy + vc = u
		// uv.worldx + vb.worldy + vc = v

		if (!solve(x1 * z1, y1 * z1, u1, x2 * z2, y2 * z2, u2, x3 * z3,
				y3 * z3, u3))
			throw new RuntimeException("Singularity u");
		float ua = a, ub = b, uc = c;

		if (!solve(x1 * z1, y1 * z1, v1, x2 * z2, y2 * z2, v2, x3 * z3,
				y3 * z3, v3))
			throw new RuntimeException("Singularity v");
		float va = a, vb = b, vc = c;

		// #2 Generate a, b, c so that the equation of the plane can be written
		// as:
		//
		// a.worldx + b.worldy + c = worldz

		if (!solve(x1 * z1, y1 * z1, z1, x2 * z2, y2 * z2, z2, x3 * z3,
				y3 * z3, z3))
			throw new RuntimeException("Singularity z");

		int texheight = texture.length / texwidth;

		for (int i = minx; i <= maxx; i++) {
			for (int j = miny; j <= maxy; j++) {

				int pixel = 0;
				int screenindex = i + width * j;

				float denom = a * i + b * j - 1;

				if (denom != 0) {

					denom = -c / denom;
					float worldx = denom * i;
					float worldy = denom * j;

					int u = (int) (ua * worldx + ub * worldy + uc);
					int v = (int) (va * worldx + vb * worldy + vc);

					if (u > 0 && u < texwidth && v > 0 && v < texheight)
						pixel = texture[u + v * texwidth];
				}
				if (screenindex >= 0 && screenindex < pixels.length)
					pixels[screenindex] = pixel;

			}
		}

		//pixels[width * y1 + x1] = pixels[width * y2 + x2] = pixels[width * y3
		//		+ x3] = 0xff00;

	}

	/*
	 * wz = a.wx + b.wy + c
	 * 
	 * sx = wx/wz sy = wy/wz
	 * 
	 * 
	 * Given: sx, sy, a, b, c unknown: wx, wy, wz
	 * 
	 * 
	 * wz = (a.sx + b.sy).wz + c
	 * 
	 * wz = -c/(a.sx + b.sy - 1)
	 */

	/**
	 * Fills a gradient triangle with colors color1, color2, color3 at vertices
	 * (x1, y1), (x2, y2), (x3, y3) respectively. Except for a few clipping
	 * issues and the fact that it draws the left edges one pixel short, this
	 * method works completely. This method should be retired in favor of the
	 * new one that will work once generalized optimal sorting, clipping, linear
	 * solving, and filling methods have been determined.
	 **/
	public static void fillGradientTriangle(int x1, int y1, int x2, int y2,
			int x3, int y3, int color1, int color2, int color3) {

		int exch = 0;

		// sort vertices by height; so that y1 < y2 < y3

		int flag = 0;

		if (y2 < y1)
			flag = 1;
		if (y3 < y1)
			flag |= 2;
		if (y3 < y2)
			flag |= 4;

		if (flag == 0) { // y2 > y1; y3 > y1; y3 > y2

		} else if (flag == 1) { // y2 < y1; y3 > y1; y3 > y2

			exch = y2;
			y2 = y1;
			y1 = exch;

			exch = x2;
			x2 = x1;
			x1 = exch;

			exch = color2;
			color2 = color1;
			color1 = exch;

			/*
			 * } else if(flag == 2) {// y2 > y1; y3 < y1, y3 > y2 This case is
			 * impossible
			 */

		} else if (flag == 3) { // y2 < y1; y3 < y1; y3 > y2 -> y1 > y3 > y2

			exch = y1;
			y1 = y2;
			y2 = y3;
			y3 = exch;

			exch = x1;
			x1 = x2;
			x2 = x3;
			x3 = exch;

			exch = color1;
			color1 = color2;
			color2 = color3;
			color3 = exch;

		} else if (flag == 4) { // y2 > y1; y3 > y1; y3 < y2 -> y2 > y3 > y1

			exch = y2;
			y2 = y3;
			y3 = exch;

			exch = x2;
			x2 = x3;
			x3 = exch;

			exch = color2;
			color2 = color3;
			color3 = exch;

			/*
			 * } else if(flag == 5) {// y2 < y1; y3 > y1; y3 < y2 This case is
			 * impossible
			 */

		} else if (flag == 6) { // y2 > y1; y3 < y1; y3 < y2; -> y2 > y1 > y3

			exch = y2;
			y2 = y1;
			y1 = y3;
			y3 = exch;

			exch = x2;
			x2 = x1;
			x1 = x3;
			x3 = exch;

			exch = color2;
			color2 = color1;
			color1 = color3;
			color3 = exch;

		} else if (flag == 7) { // y2 < y1; y3 < y1; y3 < y2

			exch = y1;
			y1 = y3;
			y3 = exch;

			exch = x1;
			x1 = x3;
			x3 = exch;

			exch = color1;
			color1 = color3;
			color3 = exch;

		} else
			throw new InternalError(
					"Sort routine failed. Check for severe hardware error.");

		// These will all be >= 0
		int dy2_1 = y2 - y1;
		int dy3_1 = y3 - y1;
		int dy3_2 = y3 - y2;

		// These may be < 0, but that's fine
		int dx2_1 = x2 - x1;
		int dx3_1 = x3 - x1;
		int dx3_2 = x3 - x2;

		float det = (dx3_1) * (dy2_1) - (dx2_1) * (dy3_1);

		if (det == 0)
			return; // Degenerate triangle; point/line

		int red1 = (color1 >> 16) & 0xff, green1 = (color1 >> 8) & 0xff, blue1 = (color1 & 0xff);
		int red2 = (color2 >> 16) & 0xff, green2 = (color2 >> 8) & 0xff, blue2 = (color2 & 0xff);
		int red3 = (color3 >> 16) & 0xff, green3 = (color3 >> 8) & 0xff, blue3 = (color3 & 0xff);

		float xred = (dy2_1) * (red3 - red1) - (dy3_1) * (red2 - red1), yred = (dx3_1)
				* (red2 - red1) - (dx2_1) * (red3 - red1);
		float xgreen = (dy2_1) * (green3 - green1) - (dy3_1)
				* (green2 - green1), ygreen = (dx3_1) * (green2 - green1)
				- (dx2_1) * (green3 - green1);
		float xblue = (dy2_1) * (blue3 - blue1) - (dy3_1) * (blue2 - blue1), yblue = (dx3_1)
				* (blue2 - blue1) - (dx2_1) * (blue3 - blue1);

		xred /= det;
		yred /= det;

		xgreen /= det;
		ygreen /= det;

		xblue /= det;
		yblue /= det;

		float zred = red1 - (x1 * xred + y1 * yred);
		float zgreen = green1 - (x1 * xgreen + y1 * ygreen);
		float zblue = blue1 - (x1 * xblue + y1 * yblue);

		int step3_1 = (dx3_1 << 12) / dy3_1;

		int left = 0;
		int right = 0;

		if (dy2_1 != 0) {

			left = (x1 << 12) + 2047;
			right = left;

			int step2_1 = (dx2_1 << 12) / dy2_1;

			if (step2_1 > step3_1) { // first part: rightStep > leftStep
				// System.out.println("part1case1");
				for (int i = y1; i < y2; i++, left += step3_1, right += step2_1) {

					int l = left >> 12;
					int r = right >> 12;

					if (l < 0)
						l = 0;
					if (r > boundx)
						r = boundx;
					if (l >= r || i < 0 || i >= height)
						continue;

					for (int j = l; j < r; j++) {

						pixels[i * width + j] = (((int) (j * xred + i * yred + zred)) << 16)
								+ (((int) (j * xgreen + i * ygreen + zgreen)) << 8)
								+ ((int) (j * xblue + i * yblue + zblue));
					}

				}

			} else {
				// System.out.println("part1case2");
				for (int i = y1; i < y2; i++, left += step2_1, right += step3_1) {

					int l = left >> 12;
					int r = right >> 12;

					if (l < 0)
						l = 0;
					if (r > boundx)
						r = boundx;
					if (l >= r || i < 0 || i >= height)
						continue;

					for (int j = l; j < r; j++) {

						pixels[i * width + j] = (((int) (j * xred + i * yred + zred)) << 16)
								+ (((int) (j * xgreen + i * ygreen + zgreen)) << 8)
								+ ((int) (j * xblue + i * yblue + zblue));
					}
				}
			}

		} else if (x1 > x2) { // skipping the above code block leaves left,
			// right uninitialized; fix that here

			left = (x2 << 12) + 2047;
			right = (x1 << 12) + 2047;

		} else {

			left = (x1 << 12) + 2047;
			right = (x2 << 12) + 2047;

		}

		if (dy3_2 != 0) {

			int step3_2 = (dx3_2 << 12) / dy3_2; // second part: rightStep <
			// leftStep

			if (step3_2 < step3_1) {
				// System.out.println("part2case1");
				for (int i = y2; i < y3; i++, left += step3_1, right += step3_2) {

					int l = left >> 12;
					int r = right >> 12;

					if (l < 0)
						l = 0;
					if (r > boundx)
						r = boundx;
					if (l >= r || i < 0 || i >= height)
						continue;

					for (int j = l; j < r; j++) {

						pixels[i * width + j] = (((int) (j * xred + i * yred + zred)) << 16)
								+ (((int) (j * xgreen + i * ygreen + zgreen)) << 8)
								+ ((int) (j * xblue + i * yblue + zblue));
					}

				}

			} else {
				// System.out.println("part2case2");
				for (int i = y2; i < y3; i++, left += step3_2, right += step3_1) {

					int l = left >> 12;
					int r = right >> 12;

					if (l < 0)
						l = 0;
					if (r > boundx)
						r = boundx;
					if (l >= r || i < 0 || i >= height)
						continue;

					for (int j = l; j < r; j++) {

						pixels[i * width + j] = (((int) (j * xred + i * yred + zred)) << 16)
								+ (((int) (j * xgreen + i * ygreen + zgreen)) << 8)
								+ ((int) (j * xblue + i * yblue + zblue));
					}
				}
			}
		}
	}

	private static int scaledWidth = 0;

	public static void solidTriangle(int X1, int Y1, int X2, int Y2, int X3,
			int Y3, int color) {
		triSort(X1, Y1, X2, Y2, X3, Y3);
		if (_y3 == _y1)
			return;

		int step31 = scaledWidth + ((_x3 - _x1) << 12) / (_y3 - _y1);

		int right = _y1 * scaledWidth, left = right;
		int boundLeft = 0, boundRight = 0;

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

					boundLeft = lineOffs[_y1];
					boundRight = boundLeft + width;

					if (l < boundLeft)
						l = boundLeft;
					if (r > boundRight)
						r = boundRight;

					while (l < r)
						pixels[l++] = color;
				}
				left += stepLeft;
				right += stepRight;
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

					boundLeft = lineOffs[_y2];
					boundRight = boundLeft + width;

					if (l < boundLeft)
						l = boundLeft;
					if (r > boundRight)
						r = boundRight;

					while (l < r)
						pixels[l++] = color;
				}

				left += stepLeft;
				right += stepRight;
				_y2++;
			}
		}

		// pixels[X1 + Y1 * width] = pixels[X2 + Y2 * width] = pixels[X3 + Y3
		// * width] = 0xffffff;
	}

	/*
	 * public static void solidTriangle(int x1, int y1, int x2, int y2, int x3,
	 * int y3, int color) { if(x1 >= 0 && y1 >= 0 && x2 >= 0 && y2 >= 0 && x3 >=
	 * 0 && y3 >= 0 && x1 < width && y1 < height && x2 < width && y2 < height &&
	 * x3 < width && y3 < height) { solidTriangleInternal(x1, y1, x2, y2, x3,
	 * y3, color); return; }
	 * 
	 * --- NONE OF THIS CODE WORKS - UNCOMMENT WHEN IT DOES
	 * 
	 * clipBufCount = clippedVertexCount = 0; emitToBuf = true; clipHorz(x1, y1,
	 * x2, y2, 0, false); clipHorz(x2, y2, x3, y3, 0, false); clipHorz(x3, y3,
	 * x1, y1, 0, false); if(clipBufCount < 3) return; emitToBuf = false;
	 * for(int i = 1; i < clipBufCount; i++) clipHorz(clipXBuf[i - 1],
	 * clipYBuf[i - 1], clipXBuf[i], clipYBuf[i], height, true);
	 * clipHorz(clipXBuf[clipBufCount - 1], clipYBuf[clipBufCount - 1],
	 * clipXBuf[0], clipYBuf[0], height, true); if(clippedVertexCount < 3)
	 * return; emitToBuf = true; for(int i = 1; i < clippedVertexCount; i++)
	 * clipVert(clippedX[i - 1], clippedY[i - 1], clippedX[i], clippedY[i], 0,
	 * false); clipVert(clippedX[clippedVertexCount - 1],
	 * clippedY[clippedVertexCount - 1], clippedX[0], clippedY[0], 0, false);
	 * if(clipBufCount < 3) return; emitToBuf = false; for(int i = 1; i <
	 * clipBufCount; i++) clipVert(clipXBuf[i - 1], clipYBuf[i - 1],
	 * clipXBuf[i], clipYBuf[i], width, true); clipVert(clipXBuf[clipBufCount -
	 * 1], clipYBuf[clipBufCount - 1], clipXBuf[0], clipYBuf[0], width, true);
	 * if(clippedVertexCount < 3) return; for(int i = 2; i < clippedVertexCount;
	 * i++) solidTriangleInternal(clippedX[0], clippedY[0], clippedX[i - 2],
	 * clippedY[i - 2], clippedX[i - 1], clippedY[i - 1], color);
	 * 
	 * 
	 * }
	 */

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

	/**
	 * Old fillGradientTriangle routine. To my knowledge, this method works
	 * perfectly under all conditions. Performance has been deemed suboptimal
	 * (Dismally slow), so research is now to be directed into the routines
	 * described above.
	 **/
	public static void fgtOLD(int x1, int y1, int x2, int y2, int x3, int y3,
			int color1, int color2, int color3) {

		if ((y1 == y2 && y2 == 3) || (x1 == x2 && x2 == x3))
			return;

		int exch = 0;

		// sort vertices by height; so that y1 < y2 < y3

		int flag = 0;

		if (y2 < y1)
			flag = 1;
		if (y3 < y1)
			flag |= 2;
		if (y3 < y2)
			flag |= 4;

		if (flag == 0) { // y2 > y1; y3 > y1; y3 > y2

		} else if (flag == 1) { // y2 < y1; y3 > y1; y3 > y2

			exch = y2;
			y2 = y1;
			y1 = exch;

			exch = x2;
			x2 = x1;
			x1 = exch;

			exch = color2;
			color2 = color1;
			color1 = exch;

			/*
			 * } else if(flag == 2) {// y2 > y1; y3 < y1, y3 > y2 This case is
			 * impossible
			 */

		} else if (flag == 3) { // y2 < y1; y3 < y1; y3 > y2 -> y1 > y3 > y2

			exch = y1;
			y1 = y2;
			y2 = y3;
			y3 = exch;

			exch = x1;
			x1 = x2;
			x2 = x3;
			x3 = exch;

			exch = color1;
			color1 = color2;
			color2 = color3;
			color3 = exch;

		} else if (flag == 4) { // y2 > y1; y3 > y1; y3 < y2 -> y2 > y3 > y1

			exch = y2;
			y2 = y3;
			y3 = exch;

			exch = x2;
			x2 = x3;
			x3 = exch;

			exch = color2;
			color2 = color3;
			color3 = exch;

			/*
			 * } else if(flag == 5) {// y2 < y1; y3 > y1; y3 < y2 This case is
			 * impossible
			 */

		} else if (flag == 6) { // y2 > y1; y3 < y1; y3 < y2; -> y2 > y1 > y3

			exch = y2;
			y2 = y1;
			y1 = y3;
			y3 = exch;

			exch = x2;
			x2 = x1;
			x1 = x3;
			x3 = exch;

			exch = color2;
			color2 = color1;
			color1 = color3;
			color3 = exch;

		} else if (flag == 7) { // y2 < y1; y3 < y1; y3 < y2

			exch = y1;
			y1 = y3;
			y3 = exch;

			exch = x1;
			x1 = x3;
			x3 = exch;

			exch = color1;
			color1 = color3;
			color3 = exch;

		} else
			throw new InternalError(
					"Sort routine failed. Check for severe hardware error.");

		// These will all be >= 0
		int dy2_1 = y2 - y1;
		int dy3_1 = y3 - y1;
		int dy3_2 = y3 - y2;

		// These may be < 0, but that's fine
		int dx2_1 = x2 - x1;
		int dx3_1 = x3 - x1;
		int dx3_2 = x3 - x2;

		int step3_1 = (dx3_1 << 12) / dy3_1;

		int left = 0;
		int right = 0;

		if (dy2_1 != 0) {

			left = x1 << 12;
			right = left;

			int step2_1 = (dx2_1 << 12) / dy2_1;

			if (step2_1 > step3_1) { // first part: rightStep > leftStep

				for (int i = y1; i < y2; i++) {

					gradientScanline(left >> 12, right >> 12, i, gradePixel(
							color1, color3, i - y1, dy3_1), gradePixel(color1,
							color2, i - y1, dy2_1));

					// System.out.println("gsl11 " + (left >> 12) + '\t' +
					// (right >> 12) + '\t' + i);

					left += step3_1;
					right += step2_1;

				}

			} else {

				for (int i = y1; i < y2; i++) {

					gradientScanline(left >> 12, right >> 12, i, gradePixel(
							color1, color2, i - y1, dy2_1), gradePixel(color1,
							color3, i - y1, dy3_1));

					// System.out.println("gsl12 " + (left >> 12) + '\t' +
					// (right >> 12) + '\t' + i);

					left += step2_1;
					right += step3_1;

				}
			}

		} else if (x1 > x2) { // skipping the above code block leaves left,
			// right uninitialized; fix that here

			left = x2 << 12;
			right = x1 << 12;

		} else {

			left = x1 << 12;
			right = x2 << 12;

		}

		if (dy3_2 != 0) {

			int step3_2 = (dx3_2 << 12) / dy3_2; // second part: rightStep <
			// leftStep

			if (step3_2 < step3_1) {

				for (int i = y2; i < y3; i++) {

					gradientScanline(left >> 12, right >> 12, i, gradePixel(
							color1, color3, i - y1, dy3_1), gradePixel(color2,
							color3, i - y2, dy3_2));

					// System.out.println("gsl21 " + (left >> 12) + '\t' +
					// (right >> 12) + '\t' + i);

					left += step3_1;
					right += step3_2;

				}

			} else {

				for (int i = y2; i < y3; i++) {

					gradientScanline(left >> 12, right >> 12, i, gradePixel(
							color2, color3, i - y2, dy3_2), gradePixel(color1,
							color3, i - y1, dy3_1));

					// System.out.println("gsl22 " + (left >> 12) + '\t' +
					// (right >> 12) + '\t' + i);

					left += step3_2;
					right += step3_1;

				}
			}
		}
	}

	private static void gradientScanline(int start, int end, int y, int startC,
			int endC) {

		if (y < 0 || y >= height)
			return;

		int count = end - start; // Compute count before bounding start/end

		if (count <= 0)
			return;

		int startoff = 0;

		if (start < 0) {

			startoff = -start;
			start = 0;

		} else if (start > boundx)
			return;

		if (end < 0)
			return;
		else if (end > boundx)
			end = boundx;

		int pixelcount = end - start;

		int sr = (startC & 0xff0000) >> 16;
		int sg = (startC & 0xff00) >> 8;
		int sb = startC & 0xff;

		int er = (endC & 0xff0000) >> 16;
		int eg = (endC & 0xff00) >> 8;
		int eb = endC & 0xff;

		int rdif = er - sr;
		int gdif = eg - sg;
		int bdif = eb - sb;

		int offset = y * width + start;

		pixelcount += startoff;

		// System.out.println("start="+start+" y="+y+" pixelcount="+pixelcount+" count="+count);

		for (int pos = startoff; pos < pixelcount; pos++) {

			int pr = sr + (pos * rdif) / count;
			int pg = sg + (pos * gdif) / count;
			int pb = sb + (pos * bdif) / count;

			pixels[offset++] = (pr << 16) + (pg << 8) + pb;

		}

	}

	public static int gradePixel(int start, int end, int pos, int count) {

		if (count == 0) {
			count = 2;
			pos = 1;
		}

		int sr = (start & 0xff0000) >> 16;
		int sg = (start & 0xff00) >> 8;
		int sb = start & 0xff;

		int er = (end & 0xff0000) >> 16;
		int eg = (end & 0xff00) >> 8;
		int eb = end & 0xff;

		int rdif = er - sr;
		int gdif = eg - sg;
		int bdif = eb - sb;

		int pr = (sr + (pos * rdif) / count) & 0xff;
		int pg = (sg + (pos * gdif) / count) & 0xff;
		int pb = (sb + (pos * bdif) / count) & 0xff;

		return (pr << 16) + (pg << 8) + pb;

	}

	public static int method317(int j, int k) {
		k = 127 - k;
		k = (k * (j & 0x7f)) / 160;
		if (k < 2)
			k = 2;
		else if (k > 126)
			k = 126;
		return (j & 0xff80) + k; // 1111111110000000
	}

	static float a = 0;
	static float b = 0;
	static float c = 0;

	/**
	 * Solves the linear system of equations
	 * 
	 * a.x1 + b.y1 + c = z1 a.x2 + b.y2 + c = z2 a.x3 + b.y3 + c = z3.
	 * 
	 * Returns true if the equation was solvable, false otherwise.
	 * 
	 * 
	 * This method uses integers, as it was determined float to integer & back
	 * conversion was extremely slow. Using integers and left & right bit
	 * shifting for multiplication/division to minimize errors was decided upon.
	 * Shift factor has not yet been determined. After it has been, this method
	 * will be implementable. A shift factor of between 12 and 16 seems the most
	 * desirable.
	 **/
	static boolean solve(int x1, int y1, int z1, int x2, int y2, int z2,
			int x3, int y3, int z3) {
		int dx12 = x1 - x2;
		int dx13 = x1 - x3;

		int dy12 = y1 - y2;
		int dy13 = y1 - y3;

		float det = dx13 * dy12 - dx12 * dy13;

		if (det == 0)
			return false;

		int dz12 = z1 - z2;
		int dz13 = z1 - z3;

		a = (dy12 * dz13 - dy13 * dz12) / det;
		b = (dx13 * dz12 - dx12 * dz13) / det;
		c = z1 - (a * x1 + b * y1);

		return true;
	}

	/**
	 * This method works under all cases. Since the usage of floats has lead to
	 * suboptimal performance, research is to be directed into the routine
	 * described above.
	 **/
	static boolean solveLinear(int x1, int y1, int z1, int x2, int y2, int z2,
			int x3, int y3, int z3) {
		int dx12 = x1 - x2;
		int dx13 = x1 - x3;

		int dy12 = y1 - y2;
		int dy13 = y1 - y3;

		float det = dx13 * dy12 - dx12 * dy13;

		if (det == 0)
			return false;

		int dz12 = z1 - z2;
		int dz13 = z1 - z3;

		a = (dy12 * dz13 - dy13 * dz12) / det;
		b = (dx13 * dz12 - dx12 * dz13) / det;
		c = z1 - (a * x1 + b * y1);

		return true;
	}

	private static void gradientLine(int pixels[], int index, int start,
			int end, int startC, int endC) {

		int count = end - start;
		if (count <= 0)
			count = -count;// return;

		int sr = (startC & 0xff0000) >> 16;
		int sg = (startC & 0xff00) >> 8;
		int sb = startC & 0xff;

		int er = (endC & 0xff0000) >> 16;
		int eg = (endC & 0xff00) >> 8;
		int eb = endC & 0xff;

		int rdif = er - sr;
		int gdif = eg - sg;
		int bdif = eb - sb;

		for (int pos = 0; pos < count; pos++) {

			int pr = (sr + (pos * rdif) / count) & 0xff;
			int pg = (sg + (pos * gdif) / count) & 0xff;
			int pb = (sb + (pos * bdif) / count) & 0xff;

			pixels[index + pos + start] = (pr << 16) + (pg << 8) + pb;

		}

	}

	/**
	 * Draws a gradient triangle based on base & height. Was used mainly to
	 * generate the triangle for http://jaghax.org/akrm/tsp.png then forgotten.
	 **/
	public static void gradientTriangle(int pixels[], int x, int y, int height,
			int baseWidth, int topColor, int leftColor, int rightColor,
			int scanSize) {

		for (int i = 0; i < height; i++) {

			int scanWidth = (baseWidth * i) / (2 * height);
			gradientLine(pixels, (i + y) * scanSize + x - scanWidth, 0,
					2 * scanWidth, gradePixel(topColor, leftColor, i, height),
					gradePixel(topColor, rightColor, i, height));

		}

	}

	// Forgot what this does ...
	public static void gradientRect(int pixels[], int x, int y, int w, int h,
			int scan, int tlCol, int trCol, int blCol, int brCol) {

		int srL = (tlCol & 0xff0000) >> 16;
		int sgL = (tlCol & 0xff00) >> 8;
		int sbL = tlCol & 0xff;

		int erL = (blCol & 0xff0000) >> 16;
		int egL = (blCol & 0xff00) >> 8;
		int ebL = blCol & 0xff;

		int rdifL = erL - srL;
		int gdifL = egL - sgL;
		int bdifL = ebL - sbL;

		int srR = (trCol & 0xff0000) >> 16;
		int sgR = (trCol & 0xff00) >> 8;
		int sbR = trCol & 0xff;

		int erR = (brCol & 0xff0000) >> 16;
		int egR = (brCol & 0xff00) >> 8;
		int ebR = brCol & 0xff;

		int rdifR = erR - srR;
		int gdifR = egR - sgR;
		int bdifR = ebR - sbR;

		// y of this scanline
		for (int i = 0; i < h; i++) {
			int prL = (srL + (i * rdifL) / h) & 0xff;
			int pgL = (sgL + (i * gdifL) / h) & 0xff;
			int pbL = (sbL + (i * bdifL) / h) & 0xff;

			int pL = (prL << 16) + (pgL << 8) + pbL;

			int prR = (srR + (i * rdifR) / h) & 0xff;
			int pgR = (sgR + (i * gdifR) / h) & 0xff;
			int pbR = (sbR + (i * bdifR) / h) & 0xff;

			int pR = (prR << 16) + (pgR << 8) + pbR;

			gradientLine(pixels, scan * (i + y), x, x + w, pL, pR);

		}

	}

	// CODE BELOW THIS LINE WAS CONTRIBUTED BY AVAIL

	// EDGE MAPPING:
	// edge1 (x1, y1) -> (x2, y2) = x1, y1, x2, y2
	// edge2 (x2, y2) -> (x3, y3) = x3, y3, x4, y4
	// edge3 (x3, y3) -> (x1, y1) = x5, y5, x6, y6
	public static void fillTriangle(int x1, int y1, int x2, int y2, int x3,
			int y3, int color) {
		// drawTriangle(x1, y1, x2, y2, x3, y3, color);
		int x4 = x3, y4 = y3, x5 = x3, y5 = y3, x6 = x1, y6 = y1, swap = 0;
		x3 = x2;
		y3 = y2;

		// sort vertices (should really be presorted)
		if (y1 > y2) {
			swap = x2;
			x2 = x1;
			x1 = swap;
			swap = y2;
			y2 = y1;
			y1 = swap;
		}
		if (y3 > y4) {
			swap = x4;
			x4 = x3;
			x3 = swap;
			swap = y4;
			y4 = y3;
			y3 = swap;
		}
		if (y5 > y6) {
			swap = x6;
			x6 = x5;
			x5 = swap;
			swap = y6;
			y6 = y5;
			y5 = swap;
		}

		int xdiff1 = x2 - x1;
		int ydiff1 = y2 - y1;
		int xdiff2 = x4 - x3;
		int ydiff2 = y4 - y3;
		int xdiff3 = x6 - x5;
		int ydiff3 = y6 - y5;
		int maxLength = Math.max(Math.max(ydiff1, ydiff2), ydiff3);
		if (maxLength == ydiff1) {
			float factor1 = (y3 - y1) / (float) ydiff1;
			float factorStep1 = 1.0f / ydiff1;
			float factor2 = 0.0f;
			float factorStep2 = 1.0f / ydiff2;
			for (; y3 < y4; y3++, factor1 += factorStep1, factor2 += factorStep2)
				drawHorizontalLine(x1 + (int) (xdiff1 * factor1), x3
						+ (int) (xdiff2 * factor2) + 1, y3, color);
			factor1 = (y5 - y1) / (float) ydiff1;
			factorStep1 = 1.0f / ydiff1;
			factor2 = 0.0f;
			factorStep2 = 1.0f / ydiff3;
			for (; y5 < y6; y5++, factor1 += factorStep1, factor2 += factorStep2)
				drawHorizontalLine(x1 + (int) (xdiff1 * factor1), x5
						+ (int) (xdiff3 * factor2) + 1, y5, color);
		} else if (maxLength == ydiff2) {
			float factor1 = (y1 - y3) / (float) ydiff2;
			float factorStep1 = 1.0f / ydiff2;
			float factor2 = 0.0f;
			float factorStep2 = 1.0f / ydiff1;
			for (; y1 < y2; y1++, factor1 += factorStep1, factor2 += factorStep2)
				drawHorizontalLine(x3 + (int) (xdiff2 * factor1), x1
						+ (int) (xdiff1 * factor2) + 1, y1, color);
			factor1 = (y5 - y3) / (float) ydiff2;
			factorStep1 = 1.0f / ydiff2;
			factor2 = 0.0f;
			factorStep2 = 1.0f / ydiff3;
			for (; y5 < y6; y5++, factor1 += factorStep1, factor2 += factorStep2)
				drawHorizontalLine(x3 + (int) (xdiff2 * factor1), x5
						+ (int) (xdiff3 * factor2) + 1, y5, color);
		} else {
			float factor1 = (y1 - y5) / (float) ydiff3;
			float factorStep1 = 1.0f / ydiff3;
			float factor2 = 0.0f;
			float factorStep2 = 1.0f / ydiff1;
			for (; y1 < y2; y1++, factor1 += factorStep1, factor2 += factorStep2)
				drawHorizontalLine(x5 + (int) (xdiff3 * factor1), x1
						+ (int) (xdiff1 * factor2) + 1, y1, color);
			factor1 = (y3 - y5) / (float) ydiff3;
			factorStep1 = 1.0f / ydiff3;
			factor2 = 0.0f;
			factorStep2 = 1.0f / ydiff2;
			for (; y3 < y4; y3++, factor1 += factorStep1, factor2 += factorStep2)
				drawHorizontalLine(x5 + (int) (xdiff3 * factor1), x3
						+ (int) (xdiff2 * factor2) + 1, y3, color);
		}
	}

	public static void drawHorizontalLine(int fromX, int toX, int y, int color) {
		if (y < 0 || y > height)
			return;
		int i = 0;
		if (fromX > toX) {
			i = toX;
			toX = fromX;
			fromX = i;
		}
		y *= width;
		fromX += y;
		toX += y;
		while (fromX < toX) {
			if (i > 0 && i < width)
				pixels[fromX] = color;
			fromX++;
			i++;
		}
	}

	// EDGE MAPPING:
	// edge1 (x1, y1) -> (x2, y2) = x1, y1, x2, y2
	// edge2 (x2, y2) -> (x3, y3) = x3, y3, x4, y4
	// edge3 (x3, y3) -> (x1, y1) = x5, y5, x6, y6

	public static void fillTriangle(int x1, int y1, int x2, int y2, int x3,
			int y3, int vertColor1, int vertColor2, int vertColor3) {
		// drawTriangle(x1, y1, x2, y2, x3, y3, vertColor1, vertColor2,
		// vertColor3);
		int x4 = x3, y4 = y3, x5 = x3, y5 = y3, x6 = x1, y6 = y1, swap = 0;
		x3 = x2;
		y3 = y2;

		// sort vertices (should really be presorted)
		if (y1 > y2) {
			swap = x2;
			x2 = x1;
			x1 = swap;
			swap = y2;
			y2 = y1;
			y1 = swap;
		}
		if (y3 > y4) {
			swap = x4;
			x4 = x3;
			x3 = swap;
			swap = y4;
			y4 = y3;
			y3 = swap;
		}
		if (y5 > y6) {
			swap = x6;
			x6 = x5;
			x5 = swap;
			swap = y6;
			y6 = y5;
			y5 = swap;
		}

		int xdiff1 = x2 - x1;
		int ydiff1 = y2 - y1;
		int xdiff2 = x4 - x3;
		int ydiff2 = y4 - y3;
		int xdiff3 = x6 - x5;
		int ydiff3 = y6 - y5;
		int maxLength = Math.max(Math.max(ydiff1, ydiff2), ydiff3);
		if (maxLength == ydiff1) {
			// System.out.println('a');
			float factor1 = (y3 - y1) / (float) ydiff1;
			float factorStep1 = 1.0f / ydiff1;
			float factor2 = 0.0f;
			float factorStep2 = 1.0f / ydiff2;
			float gradDist = y4 + y6;
			int i = 1, j = 0;
			for (; y3 < y4; y3++, factor1 += factorStep1, factor2 += factorStep2)
				horizontalGradient(x1 + (int) (xdiff1 * factor1), x3
						+ (int) (xdiff2 * factor2) + 1, y3, grade(vertColor2,
						vertColor1, i++, gradDist), grade(vertColor2,
						vertColor3, y3, y4));
			factor1 = (y5 - y1) / (float) ydiff1;
			factorStep1 = 1.0f / ydiff1;
			factor2 = 0.0f;
			factorStep2 = 1.0f / ydiff3;
			for (j = 0; y5 < y6; y5++, factor1 += factorStep1, factor2 += factorStep2)
				horizontalGradient(x1 + (int) (xdiff1 * factor1), x5
						+ (int) (xdiff3 * factor2) + 1, y5, grade(vertColor2,
						vertColor1, i++, gradDist), grade(vertColor3,
						vertColor1, j++, y6));
		} else if (maxLength == ydiff2) {
			// System.out.println('b');
			float factor1 = (y1 - y3) / (float) ydiff2;
			float factorStep1 = 1.0f / ydiff2;
			float factor2 = 0.0f;
			float factorStep2 = 1.0f / ydiff1;
			float gradDist = y2 + y6;
			int i = 1, j = 0;
			for (; y1 < y2; y1++, factor1 += factorStep1, factor2 += factorStep2)
				horizontalGradient(x3 + (int) (xdiff2 * factor1), x1
						+ (int) (xdiff1 * factor2) + 1, y1, grade(vertColor2,
						vertColor1, i++, gradDist), grade(vertColor3,
						vertColor1, y1, y2));
			factor1 = (y5 - y3) / (float) ydiff2;
			factorStep1 = 1.0f / ydiff2;
			factor2 = 0.0f;
			factorStep2 = 1.0f / ydiff3;
			System.out.println("factor1=" + factor1 + " factorStep1="
					+ factorStep1 + " factorStep2=" + factorStep2);
			for (j = 0; y5 < y6; y5++, factor1 += factorStep1, factor2 += factorStep2)
				horizontalGradient(x3 + (int) (xdiff2 * factor1), x5
						+ (int) (xdiff3 * factor2) + 1, y5, grade(vertColor2,
						vertColor1, i++, gradDist), grade(vertColor2,
						vertColor3, j++, y6));
		} else {
			// System.out.println('c');
			float factor1 = (y1 - y5) / (float) ydiff3;
			float factorStep1 = 1.0f / ydiff3;
			float factor2 = 0.0f;
			float factorStep2 = 1.0f / ydiff1;
			float gradDist = y2 + y4;
			int i = 1, j = 0;
			for (; y1 < y2; y1++, factor1 += factorStep1, factor2 += factorStep2)
				horizontalGradient(x5 + (int) (xdiff3 * factor1), x1
						+ (int) (xdiff1 * factor2) + 1, y1, grade(vertColor1,
						vertColor3, i++, gradDist), grade(vertColor1,
						vertColor2, y1, y2));
			factor1 = (y3 - y5) / (float) ydiff3;
			factorStep1 = 1.0f / ydiff3;
			factor2 = 0.0f;
			factorStep2 = 1.0f / ydiff2;
			for (j = 0; y3 < y4; y3++, factor1 += factorStep1, factor2 += factorStep2)
				horizontalGradient(x5 + (int) (xdiff3 * factor1), x3
						+ (int) (xdiff2 * factor2) + 1, y3, grade(vertColor1,
						vertColor3, i++, gradDist), grade(vertColor2,
						vertColor3, j++, y4));
		}
	}

	public static void horizontalGradient(int fromX, int toX, int y, int fc,
			int tc) {
		if (y < 0 || y > height)
			return;
		int i = 0;
		if (fromX > toX) {
			i = toX;
			toX = fromX;
			fromX = i;
		}
		float xdiff = toX - fromX;
		y *= width;
		fromX += y;
		toX += y;
		while (fromX < toX) {
			if (i > 0 && i < width)
				pixels[fromX] = grade(fc, tc, i, xdiff);
			fromX++;
			i++;
		}
	}

	private static int grade(int fromColor, int toColor, float numer,
			float denom) {
		float percent = numer / denom;
		// System.out.println("Percent = " + percent);
		int fr = (fromColor >> 16) & 0xff;
		int fg = (fromColor >> 8) & 0xff;
		int fb = fromColor & 0xff;
		int tr = (toColor >> 16) & 0xff;
		int tg = (toColor >> 8) & 0xff;
		int tb = toColor & 0xff;
		int dr = tr - fr, dg = tg - fg, db = tb - fb;
		dr *= percent;
		dg *= percent;
		db *= percent;
		dr += fr;
		dg += fg;
		db += fb;
		return (dr << 16) + (dg << 8) + db;
	}
}