package com.glgames.game;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.net.URL;

public class CacheReader {
	private DataInputStream dis;
	
	public CacheReader(String fn) {
		try {
			dis = new DataInputStream(new FileInputStream(fn));
		} catch(Exception e) {
			try {
				dis = new DataInputStream(new URL(
						"http://jaghax.org/akrm/icepush/" + fn).openStream());
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	
	public byte readByte() {
		try {
			return dis.readByte();
		} catch (Exception e) {
			return -1;
		}
	}
	
	public short readShort() {
		try {
			return dis.readShort();
		} catch (Exception e) {
			return -1;
		}
	}
	
	public int readInt() {
		try {
			return dis.readInt();
		} catch (Exception e) {
			return -1;
		}
	}
	
	public String readString() {
		try {
			int len = dis.readShort();
			byte[] strb = new byte[len];
			dis.readFully(strb);
			return new String(strb);
		} catch(Exception e) {
			return "null";
		}
	}
}
