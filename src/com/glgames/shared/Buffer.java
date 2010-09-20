package com.glgames.shared;
public class Buffer {

	public byte[] inBuf;
	public int readPtr;

	public byte[] outBuf;
	public int writePtr;

	public Buffer() {
		inBuf = new byte[5000];
		outBuf = new byte[5000];
	}

	public void writeByte(int b) {
		outBuf[writePtr++] = (byte) (b & 0xff);
	}

	public void writeShort(int s) {
		outBuf[writePtr++] = (byte) (s & 0xff);
		outBuf[writePtr++] = (byte) ((s >> 8) & 0xff);
	}

	public void writeInt(int i) {
		outBuf[writePtr++] = (byte) (i & 0xff);
		outBuf[writePtr++] = (byte) ((i >> 8) & 0xff);
		outBuf[writePtr++] = (byte) ((i >> 16) & 0xff);
		outBuf[writePtr++] = (byte) ((i >> 24) & 0xff);
	}

	public void writeString(String str) {
		int len = str.length();
		writeShort(len);
		System.arraycopy(str.getBytes(), 0, outBuf, writePtr, len);
		writePtr += len;
	}

	public int readByte() {
		return inBuf[readPtr++];
	}

	public int readShort() {
		int s = (0xff & inBuf[readPtr]) + (inBuf[readPtr + 1] << 8);
		readPtr += 2;
		return s;
	}

	public int readInt() {
		int i = (0xff & inBuf[readPtr]) + ((0xff & inBuf[readPtr + 1]) << 8)
				+ ((0xff & inBuf[readPtr + 2]) << 16)
				+ ((0xff & inBuf[readPtr + 3]) << 24);
		readPtr += 4;
		return i;
	}

	public String readString() {
		int len = readShort();
		String str = new String(inBuf, readPtr, len);
		readPtr += len;
		return str;
	}
}