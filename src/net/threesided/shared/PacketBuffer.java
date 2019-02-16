package net.threesided.shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class PacketBuffer extends Buffer {

    // TODO: Make this consistent with the rest of the opcodes
    protected static final int PING = -37;

    protected Socket socket;

    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected int pktStart;
    protected int pktEnd;
    protected int dataEnd;
    protected long lastWriteTime;
    protected long lastReadTime;

    // Amount of time to send a ping after if no packets have been sent
    public int pingTime = 3000;

    //	Amount of time to drop the connection if no packets are received
    public int pingTimeout = 7000;
    public static boolean debug = true;

    public PacketBuffer(final Socket s) {
        this.socket = s;
        try {
            this.inputStream = this.socket.getInputStream();
            this.outputStream = this.socket.getOutputStream();
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        this.lastReadTime = this.lastWriteTime = System.currentTimeMillis();
    }

    public void beginPacket(final int opcode) {
        this.pktStart = this.writePtr;
        // Reserve two bytes at the beginning for the size, which can't be computed until endPacket()
        this.writePtr += 2;
        this.writeByte(opcode);
    }

    /**
     * Places size of packet inside packet header (prepares for sending).
     */
    public void endPacket() {
        final int saveWritePtr = this.writePtr;
        this.writePtr = this.pktStart;
        // Write size to the reserved space: size = current - start.
        this.writeShort(saveWritePtr - this.pktStart);
        this.writePtr = saveWritePtr;
    }

    public int openPacket() {
        if ((this.readPtr + 3) > this.dataEnd) {
            // Smallest valid packet size is 3
            return -1;
        }
        final int size = readShort();
        if (size < 0) {
            // hax
            return -1;
        }
        // Reset the read ptr in case the packet is incomplete
        this.readPtr -= 2;
        // end = current + size;
        this.pktEnd = this.readPtr + size;
        if (this.pktEnd > this.dataEnd) {
            return -1;
        }
        // Skip the length bytes
        this.readPtr += 2;
        final int op = this.readByte();
        if (op == Constants.PING) {
            return -1;
        }
        return op;
    }

    public int remaining() {
        return this.pktEnd - this.readPtr;
    }

    /**
     * Discards unread data.
     */
    public void closePacket() {
        this.readPtr = this.pktEnd;
    }

    /**
     * Sends the packet across the network.
     * @return If the connection is still usable.
     */
    public boolean sync() {
        boolean result = true;
        final long time = System.currentTimeMillis();
        try {
            if (time - this.lastWriteTime >= this.pingTime) {
                this.beginPacket(Constants.PING);
                this.endPacket();
            }

            if (this.writePtr != 0) {
                this.outputStream.write(this.outBuf, 0, this.writePtr);
                this.outputStream.flush();
                this.lastWriteTime = time;
            }
            this.writePtr = 0;
        } catch (final IOException ex) {
            ex.printStackTrace();
            result = false;
        }
        try {
            final int numBytesUnread = this.dataEnd - this.readPtr;
            final int available = this.inputStream.available();
            if (numBytesUnread + available < 0) {
                return false;
            }
            final byte[] newInBuf = new byte[numBytesUnread + available];
            if (numBytesUnread != 0) {
                System.arraycopy(this.inBuf, this.readPtr, newInBuf, 0, numBytesUnread);
            }
            this.dataEnd = numBytesUnread;
            if (available != 0) {
                this.lastReadTime = System.currentTimeMillis();
                final int bytesRead = this.inputStream.read(newInBuf, numBytesUnread, available);
                this.dataEnd += bytesRead;
            } else if ((time - this.lastReadTime) >= this.pingTimeout) {
                System.out.println("Ping timeout expired: " + (time - this.lastReadTime));
                return false;
            }
            this.inBuf = newInBuf;
            this.readPtr = 0;
        } catch (final IOException ex) {
            ex.printStackTrace();
            result = false;
        }
        return result;
    }

    public void shutdown() {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    protected void finalize() {
        this.outBuf = null;
        this.inBuf = null;
        this.inputStream = null;
        this.outputStream = null;
    }
}
