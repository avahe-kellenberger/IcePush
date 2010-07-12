package com.glgames.shared;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FileBuffer {
	private byte[] file;
	private int ptr;
	
	public FileBuffer(String fn) {
		try {
			Socket s = new Socket(Opcodes.WORLDSERVER, Opcodes.WORLDPORT);
			InputStream in = s.getInputStream();
			OutputStream out = s.getOutputStream();
			out.write(Opcodes.REQUEST_FILE);
			out.write(fn.length());
			out.write(fn.getBytes());
			out.flush();
			int len = in.read() | (in.read() << 8) | (in.read() << 16) | (in.read() << 24);
			file = new byte[len];
			ptr = 0;
			in.read(file);
			in.close();
			out.close();
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
		return (short) ((file[ptr++] << 8) | file[ptr++]);
	}
	
	public int readInt() {
		return (file[ptr++] << 24) | (file[ptr++] << 16) 
				| (file[ptr++] << 8) | file[ptr++];
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
		file[ptr++] = (byte) ((s >> 8) & 0xff);
		file[ptr++] = (byte) (s & 0xff);
	}
	
	public void writeInt(int i) {
		file[ptr++] = (byte) ((i >> 24) & 0xff);
		file[ptr++] = (byte) ((i >> 16) & 0xff);
		file[ptr++] = (byte) ((i >> 8) & 0xff);
		file[ptr++] = (byte) (i & 0xff);
	}
	
	public void writeString(String s) {
		int len = s.length();
		writeShort(len);
		System.arraycopy(s.getBytes(), 0, file, ptr, len);
		ptr += len;
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
	}
}
