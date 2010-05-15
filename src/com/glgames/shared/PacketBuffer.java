package com.glgames.shared;

import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PacketBuffer {
	private Socket sock;

	private InputStream in;
	private byte[] inBuf;
	private int readPtr;
	private int pktEnd;
	private int dataEnd;

	private OutputStream out;
	private byte[] outBuf;
	private int writePtr;
	private int pktStart;

	private long lastReadTime;

	public static boolean debug = false;

	public PacketBuffer(Socket s) {
		sock = s;
		try {
			in = sock.getInputStream();
			out = sock.getOutputStream();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		inBuf = new byte[5000];
		outBuf = new byte[5000];

		lastReadTime = System.currentTimeMillis();

	}

	public void beginPacket(int opcode) {
		debug("PacketBuffer::beginPacket[0]");
		pktStart = writePtr;
		writePtr += 2; // Reserve a byte of space at the beginning for the size
		// byte; which can't be computed until EndPacket()
		writeByte(opcode);
		debug("PacketBuffer::beginPacket[1]");
	}

	public void endPacket() {
		debug("PacketBuffer::endPacket[0]");
		int saveWritePtr = writePtr;
		writePtr = pktStart;
		writeShort(saveWritePtr - pktStart); // Write the size to the reserved
		// space: size = curent - start.
		writePtr = saveWritePtr;
		debug("PacketBuffer::endPacket[1]");
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

	public int openPacket() {
		debug("PacketBuffer::openPacket[0]");
		// new Exception().printStackTrace();
		if ((readPtr + 3) > dataEnd) {
			debug("PacketBuffer::openPacket[1]");
			return -1; // Smallest valid packet size is 3
		}
		int size = readShort();
		debug("Opened packet size = " + size);
		readPtr -= 2; // Reset the read ptr in case the packet is incomplete
		pktEnd = readPtr + size; // end = current + size;
		if (pktEnd > dataEnd) {
			debug("PacketBuffer::openPacket[2]");
			return -1;
		}
		readPtr += 2; // Skip the length bytes
		debug("PacketBuffer::openPacket[3]");
		return readByte();
	}

	public int remaining() {
		return pktEnd - readPtr;
	}

	public void closePacket() {
		if (debug)
			System.out.println("PacketBuffer::closePacket");
		readPtr = pktEnd;
	}

	public boolean synch() {
		boolean result = true;
		try {
			if (writePtr != 0) {
				out.write(outBuf, 0, writePtr);
				out.flush();
				if (debug)
					System.out
							.println("[[[ PACKET DATA HAS BEEN SENT TO PEER ]]]");
			}
			// System.out.println("writePtr = " + writePtr);
			writePtr = 0;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			result = false;
		}
		try {
			int numBytesUnread = dataEnd - readPtr;
			// if(numBytesUnread < 0) {
			// System.out.println("Something broke: " + dataEnd + ", " +
			// readPtr);
			// numBytesUnread = 0;
			// }
			int available = in.available();
			// System.out.println("available = " + available);
			byte[] newInBuf = new byte[numBytesUnread + available];
			if (numBytesUnread != 0) {
				if (debug)
					System.out.println("Unread bytes in read buffer: "
							+ numBytesUnread);
				System.arraycopy(inBuf, readPtr, newInBuf, 0, numBytesUnread);
			}
			dataEnd = numBytesUnread;
			if (available != 0) {
				lastReadTime = System.currentTimeMillis();
				int bytesRead = in.read(newInBuf, numBytesUnread, available);
				dataEnd += bytesRead;
				if (debug)
					System.out.println(" SYNCH bytesRead = " + bytesRead);
				/* ALL CODE IN COMMENTS HAS BEEN COMMENTED OUT */
			} else {
				if (debug)
					System.out.println("[SYNCH] NO BYTES SENT FROM PEER");
				long time = System.currentTimeMillis();
				if ((time - lastReadTime) >= 3000) {
					System.out
							.println("Over three seconds since data was send: "
									+ (time - lastReadTime));
					return false;
				}
			}
			inBuf = newInBuf;
			readPtr = 0;
			// System.out.println("dataEnd = " + dataEnd + " num bytes read: " +
			// (dataEnd - numBytesUnread) + " numBytesUnread: " +
			// numBytesUnread);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			result = false;
		}
		return result;
	}

	public void shutdown() {
		try {
			if (sock != null)
				sock.close();
		} catch (IOException ioex_) {
			ioex_.printStackTrace();
		}
	}

	public void debug(String blah) {
		if (debug) {
			System.out.println(blah);
			System.out.println("--- readPtr=" + readPtr + " pktEnd=" + pktEnd
					+ " writePtr=" + writePtr + " pktStart=" + pktStart
					+ " remaining=" + remaining() + " dataEnd=" + dataEnd);
			System.out.println(" --- OUTGOING PACKET DATA --- ");
			System.out.print("{");
			for (int i = 0; i < writePtr; i++)
				System.out.print(outBuf[i] + ",");
			System.out.println("}");

			if (inBuf.length < 5000) {
				System.out.println(" --- RECIEVED PACKET DATA --- ");
				System.out.print("{");
				for (int i : inBuf)
					System.out.print(i + ",");
				System.out.println("}");
			}

			if (pktEnd < readPtr)
				System.out
						.println(" **** PKTEND IS LESS THEN READPTR **** ");
		}
	}

	protected void finalize() {
		outBuf = null;
		inBuf = null;
		in = null;
		out = null;
	}
}
