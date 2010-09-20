package com.glgames.shared;

import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PacketBuffer extends Buffer {
	protected Socket sock;

	protected InputStream in;
	protected int pktEnd;
	protected int dataEnd;

	protected static final int PING = -37; 		// TODO: Make this consistent with the rest of the opcodes

	protected OutputStream out;
	protected int pktStart;

	protected long lastWriteTime;
	protected long lastReadTime;

	public int pingTime	= 3000;		//	Amount of time to send a ping after if no packets have been sent
	public int pingTimeout	= 7000;		//	Amount of time to drop the connection if no packets are recieved

	public static boolean debug = true;

	public PacketBuffer(Socket s) {
		sock = s;
		try {
			in = sock.getInputStream();
			out = sock.getOutputStream();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		lastReadTime = lastWriteTime = System.currentTimeMillis();

	}

	public void beginPacket(int opcode) {
		pktStart = writePtr;
		writePtr += 2;					// Reserve two bytes at the beginning for the size, which can't be computed until endPacket()
		writeByte(opcode);
	}

	public void endPacket() {
		int saveWritePtr = writePtr;
		writePtr = pktStart;
		writeShort(saveWritePtr - pktStart);	// Write size to the reserved space: size = curent - start.
		writePtr = saveWritePtr;
	}

	public int openPacket() {
		if ((readPtr + 3) > dataEnd) {
			return -1; // Smallest valid packet size is 3
		}
		int size = readShort();
		if(size < 0) return -1;			// hax
		readPtr -= 2; // Reset the read ptr in case the packet is incomplete
		pktEnd = readPtr + size; // end = current + size;
		if (pktEnd > dataEnd) {
			return -1;
		}
		readPtr += 2; // Skip the length bytes
		int op = readByte();
		if(op == PING) return -1;
		return op;
	}

	public int remaining() {
		return pktEnd - readPtr;
	}

	public void closePacket() {
		readPtr = pktEnd;
	}

	public boolean synch() {
		boolean result = true;
		long time = System.currentTimeMillis();
		try {
			if(time - lastWriteTime >= pingTime) {
				beginPacket(PING);
				endPacket();
			}

			if (writePtr != 0) {
				out.write(outBuf, 0, writePtr);
				out.flush();
				lastWriteTime = time;
			}
			writePtr = 0;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			result = false;
		}
		try {
			int numBytesUnread = dataEnd - readPtr;
			int available = in.available();
			if(numBytesUnread + available < 0)
				return false;
			byte[] newInBuf = new byte[numBytesUnread + available];
			if (numBytesUnread != 0) {
				System.arraycopy(inBuf, readPtr, newInBuf, 0, numBytesUnread);
			}
			dataEnd = numBytesUnread;
			if (available != 0) {
				lastReadTime = System.currentTimeMillis();
				int bytesRead = in.read(newInBuf, numBytesUnread, available);
				dataEnd += bytesRead;
			} else {
				if ((time - lastReadTime) >= pingTimeout) {
					System.out.println("Ping timeout expired: " + (time - lastReadTime));
					return false;
				}
			}
			inBuf = newInBuf;
			readPtr = 0;
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

	protected void finalize() {
		outBuf = null;
		inBuf = null;
		in = null;
		out = null;
	}
}
