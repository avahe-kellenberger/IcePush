package com.glgames.test;

import java.net.Socket;
import java.io.IOException;

import com.glgames.shared.PacketBuffer;

public class DebugPacketBuffer extends PacketBuffer {

	public DebugPacketBuffer(Socket s) {
		super(s);
	}

	public void beginPacket(int opcode) {
		debugOut("PacketBuffer::beginPacket[0] opcode: " + opcode);
		super.beginPacket(opcode);
		debugOut("PacketBuffer::beginPacket[1]");
	}

	public void endPacket() {		
		debugOut("PacketBuffer::endPacket[0]");
		super.endPacket();
		debugOut("PacketBuffer::endPacket[1]");
	}

	public int openPacket() {
		debugIn("PacketBuffer::openPacket[0]");
		// new Exception().printStackTrace();
		if ((readPtr + 3) > dataEnd) {
			debugIn("PacketBuffer::openPacket[1]");
			return -1; // Smallest valid packet size is 3
		}
		int size = readShort();
		if(size < 0) return -1;			// hax
		debugIn("Opened packet size = " + size);
		readPtr -= 2; // Reset the read ptr in case the packet is incomplete
		pktEnd = readPtr + size; // end = current + size;
		if (pktEnd > dataEnd) {
			debugIn("PacketBuffer::openPacket[2]");
			return -1;
		}
		readPtr += 2; // Skip the length bytes
		debugIn("PacketBuffer::openPacket[3]");
		return readByte();
	}

	public void debugIn(String blah) {
		System.out.print('\t' + blah + ":  ");
		System.out.println("readPtr=" + readPtr + " pktEnd=" + pktEnd + " dataEnd=" + dataEnd);
		System.out.print("\t\tIncoming data: ");
		printArray(inBuf, dataEnd);
		System.out.println();
	}

	public void debugOut(String blah) {
		System.out.print('\t' + blah + ": ");
		System.out.println("writePtr=" + writePtr + " pktStart=" + pktStart);
		System.out.print("\t\tOutgoing data: ");
		printArray(outBuf, writePtr);
		System.out.println();
	}

	// Code adapted from http://www.docjar.com/html/api/java/util/Arrays.java.html.
 	public static void printArray(byte[] array, int len) {
 		if (array == null) {
			System.out.println("null"); //$NON-NLS-1$
			return;
		}
		if (len == 0) {
			System.out.println("{}"); //$NON-NLS-1$
			return;
		}
		StringBuilder sb = new StringBuilder(2 + array.length * 4);
		sb.append('{');
		sb.append(array[0]);
		for (int i = 1; i < len; i++) {
			sb.append(", "); //$NON-NLS-1$
			sb.append(array[i]);
		}
		sb.append('}');
		System.out.println(sb);
	}
}