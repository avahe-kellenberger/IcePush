package com.glgames.shared;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class FileBuffer {
	static final String BASE_URL = "http://strictfp.com/icepush/";
	private byte[] file;
	private int ptr;
	
	public FileBuffer(String fn) {
		try {
			URLConnection con = new URL(BASE_URL + fn).openConnection();
			file = new byte[con.getContentLength()];
			ptr = 0;
			InputStream in = con.getInputStream();
			in.read(file);
			in.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public FileBuffer(byte[] f) {
		file = f;
		ptr = 0;
	}
	
	public byte readByte() {
		return file[ptr++];
	}
	
	public short readShort() {
		short s = (short) ((file[ptr++] << 8) | (file[ptr++] & 0xff));
		return s;
	}
	
	public int readInt() {
		return (file[ptr++] << 24) | ((file[ptr++] << 16) & 0xff) 
				| ((file[ptr++] << 8) & 0xff) | (file[ptr++] & 0xff);
	}
	
	public String readString() {
		int len = readShort();
		byte[] b = new byte[len];
		System.arraycopy(file, ptr, b, 0, len);
		ptr += len;
		return new String(b);
	}
	
	public void writeByte(int i) {
		file[ptr++] = (byte) i;
	}
	
	public void writeBytes(byte[] b, int off, int len) {
		for(int k = off; k < off + len; k++)
			file[ptr++] = b[k];
	}
	
	public void writeShort(int s) {
		file[ptr++] = (byte) (s >> 8);
		file[ptr++] = (byte) (s     );
	}
	
	public void writeInt(int i) {
		file[ptr++] = (byte) (i >> 24);
		file[ptr++] = (byte) (i >> 16);
		file[ptr++] = (byte) (i >>  8);
		file[ptr++] = (byte) (i      );
	}
	
	public void writeString(String s) {
		int len = s.length();
		writeShort(len);
		System.arraycopy(s.getBytes(), 0, file, ptr, len);
		ptr += len;
	}
	
	public void writeColor(Color c) {
		writeByte(c.getAlpha());
		writeByte(c.getRed());
		writeByte(c.getGreen());
		writeByte(c.getBlue());
	}
	
	public Color readColor() {
		int a = readByte(), r = readByte(), g = readByte(), b = readByte();
		a &= 0xff;
		r &= 0xff;
		g &= 0xff;
		b &= 0xff;
		return new Color(r, g, b, a);
	}
	
	public void save(String fn) {
		try {
			FileOutputStream out = new FileOutputStream(fn);
			out.write(file, 0, ptr);
			out.flush();
			out.close();
			debug();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void debug() {
		System.out.println("at: " + ptr + ", total: " + file.length);
		System.out.println();
	}
}
