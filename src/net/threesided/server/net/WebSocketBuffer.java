package net.threesided.server.net;

import net.threesided.shared.Constants;
import net.threesided.shared.PacketBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class WebSocketBuffer extends PacketBuffer {

    private static final String responseHeader =
            "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n";
    private static long maxTime = 5000;

    private int headerMax = 10000;
    private byte[] header = new byte[headerMax];
    private boolean headerRead = false;
    private long startTime;
    private int headerPos = 0;

    // Contains any left over bytes from incoming websocket frames that are incomplete
    private byte[] rawInBuff;
    private int nextPos;
    private boolean readVer;
    private boolean readName;

    private char get64(int i) {
        if (i >= 0 && i <= 25) {
            return (char) ('A' + i);
        }
        if (i >= 26 && i <= 51) {
            return (char) ('a' + (i - 26));
        }
        if (i >= 52 && i <= 61) {
            return (char) ('0' + (i - 52));
        }

        if (i == 62) {
            return '+';
        }

        if (i == 63) {
            return '/';
        }
        return '~';
    }

    public int openPacket() {
        if ((this.readPtr + 3) > this.dataEnd) {
            // Smallest valid packet size is 3
            return -1;
        }
        final int size = readShort();
        if (size < 0) {
            this.readPtr -= 2;
            return -1; // hax
        }
        // Reset the read ptr in case the packet is incomplete
        this.readPtr -= 2;
        this.pktEnd = this.readPtr + size;
        if (this.pktEnd > this.dataEnd) {
            return -1;
        }
        // Skip the length bytes
        this.readPtr += 2;
        final int op = readByte();
        if (op == Constants.PING) {
            return -1;
        }
        return op;
    }

    private String base64(byte[] b) {
        final StringBuilder stringBuilder = new StringBuilder();
        int i = 0, c = 0;
        int bits = 0;

        while (i != b.length) {
            c = i % 3;

            if (c == 0) {
                stringBuilder.append(get64((b[i] >> 2) & 63));
                bits = (b[i] & 3) << 4;
            }

            if (c == 1) {
                stringBuilder.append(get64(bits | ((b[i] >> 4) & 15)));
                bits = (b[i] & 15) << 2;
            }

            if (c == 2) {
                stringBuilder.append(get64(bits | ((b[i] >> 6) & 3)));
                stringBuilder.append(get64(b[i] & 63));
                bits = 0;
            }
            i++;
        }

        if (c != 2) {
            stringBuilder.append(get64(bits));
        }

        if (c == 1) {
            stringBuilder.append('=');
        }
        if (c == 0) {
            stringBuilder.append("==");
        }

        return stringBuilder.toString();
    }

    public int available() {
        return this.inBuf.length - this.readPtr;
    }

    private String generateKey(String str) {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA-1");

            final String magic = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            final String reply = str + magic;
            final byte[] b = reply.getBytes();
            sha.update(b, 0, b.length);
            return base64(sha.digest());
        } catch (final NoSuchAlgorithmException ignored) {
        }
        return null;
    }

    private boolean readHeader(final Socket socket) throws IOException {
        byte[] buf = this.header;

        final InputStream inputStream = socket.getInputStream();
        final int a = inputStream.available();

        if (a + this.headerPos > this.headerMax || a < 1) {
            return false;
        }

        final int read = inputStream.read(buf, this.headerPos, a);
        if (read == 0 || read == -1) {
            return false;
        }

        final int end = this.headerPos = this.headerPos + read;
        final byte[] term = "\r\n\r\n".getBytes();
        int headerEnd = -1;
        for (int i = 0; i < end; i++) {
            if (buf[i] == term[0]
                    && buf[i + 1] == term[1]
                    && buf[i + 2] == term[2]
                    && buf[i + 3] == term[3]) {
                headerEnd = i;
            }
        }

        // This code only executes if the header ending was found.
        if (headerEnd != -1) {
            int rem = buf.length - (headerEnd + 4);
            byte[] nbuf = new byte[rem];
            System.arraycopy(buf, headerEnd + 4, nbuf, 0, rem);
            String header = new String(buf, 0, headerEnd);

            String[] lines = header.split("\r\n");
            boolean foundKey = false;
            for (String line : lines) {
                final String[] parts = line.split("Sec-WebSocket-Key: ");
                if (parts.length == 2) {
                    final String swsk = parts[1];
                    final String response = WebSocketBuffer.responseHeader + "Sec-WebSocket-Accept: " + generateKey(swsk) + "\r\n\r\n";
                    socket.getOutputStream().write(response.getBytes());
                    foundKey = true;
                    break;
                }
            }
            return foundKey;
        } else {
            System.out.println("WebSocketBuffer: Header end not found");
            return false;
        }

        // return true;
    }

    public WebSocketBuffer(Socket socket) {
        super(socket);
        this.rawInBuff = new byte[0];
        this.startTime = System.currentTimeMillis();
        try {
            this.headerRead = this.readHeader(socket);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    private byte[] readOneFrame(final byte[] data, final int pos, final int end) {
        if (end < pos + 2) {
            return null;
        }

        int opcode = data[pos] & 15;
        int mask = (data[pos + 1] >> 7) & 1;
        long dataLen = data[pos + 1] & 127;
        int bonus = 0;
        int fin = (data[pos] >> 7) & 1;

        if (mask != 1) {
            throw new RuntimeException("Unmasked frame");
        }

        if (dataLen == 126) {
            bonus = 2;
            if (end < pos + 4) {
                return null;
            }
            dataLen = ((data[pos + 2] & 0xff) << 8) + (data[pos + 3] & 0xff);
        } else if (dataLen == 127) {
            if (end < pos + 10) {
                return null;
            }
            bonus = 8;
        }

        if (end < (6 + pos + bonus + dataLen)) {
            return null;
        }

        final byte[] key = new byte[] {
                data[pos + 2 + bonus],
                data[pos + 3 + bonus],
                data[pos + 4 + bonus],
                data[pos + 5 + bonus]
        };


        final byte nbuf[] = new byte[(int) dataLen];
        for (int i = 0; i < dataLen; i++) {
            nbuf[i] = (byte) (key[i % 4] ^ data[pos + 6 + i + bonus]);
        }

        this.nextPos = pos + 6 + (int) dataLen + bonus;
        return nbuf;
    }

    byte[] readAllFrames(byte[] data) {
        byte[][] matrix = new byte[1 + (data.length / 6)][];
        int total = 0;
        byte[] curr = readOneFrame(data, 0, data.length);
        int i = 0;
        while (curr != null) {
            total = total + curr.length;
            matrix[i] = curr;
            i++;
            curr = readOneFrame(data, nextPos, data.length);
        }
        // System.out.println("Total payload data read: " + total);

        if (nextPos < data.length) {
            byte[] newRaw = new byte[data.length - nextPos];
            System.arraycopy(data, nextPos, newRaw, 0, newRaw.length);
            rawInBuff = newRaw;
        }

        if (nextPos == data.length) {
            rawInBuff = new byte[0];
        }

        if (i != 0) {
            byte[] result = new byte[total];
            int j = 0;
            for (byte[] b : matrix) {
                if (b != null) {
                    System.arraycopy(b, 0, result, j, b.length);
                    j = j + b.length;
                }
            }

            return result;
        }
        return null;
    }

    private byte[] encodeData(final byte[] data, final int len) {
        final int packs = len / 125;
        final int tail = len % 125;
        // 125 is maximum size of a data frame without extended payload length bytes
        final byte[] out = new byte[(packs) * (2 + 125) + (tail == 0 ? 0 : (2+ tail))];

        for (int i = 0; i < packs; i++) {
            // opcode = 2 for binary data, FIN = 1
            out[i * 127] = (byte) (((byte) 1) << 7) | (2);
            // Data length
            out[i * 127 + 1] = 125;
            System.arraycopy(data, i * 125, out, i * 127 + 2, 125);
        }

        if (tail != 0) {
            out[packs * 127] = (byte) (((byte) 1) << 7) | (2);
            // Data length is number of bytes remaining in the tail
            out[packs * 127 + 1] = (byte) tail;
            System.arraycopy(data, packs * 125, out, packs * 127 + 2, tail);
        }
        return out;
    }

    public boolean sync() {
        final long time = System.currentTimeMillis();
        if (!this.headerRead) {
            try {
                this.headerRead = readHeader(this.socket);
            } catch (final IOException ex) {
                ex.printStackTrace();
                return false;
            }
            if (!this.headerRead) {
                return (time - this.startTime) < WebSocketBuffer.maxTime;
            }
        }

        boolean result = true;

        // Write out all data in the out buffers
        try {
            if (time - this.lastWriteTime >= this.pingTime) {
                beginPacket(Constants.PING);
                this.endPacket();
            }

            if (writePtr != 0) {
                this.outputStream.write(encodeData(this.outBuf, this.writePtr));
                this.outputStream.flush();
                this.lastWriteTime = time;
            }
            this.writePtr = 0;
        } catch (final IOException ex) {
            ex.printStackTrace();
            result = false;
        }
        try {
            final int available = this.inputStream.available();
            if (available < 0) {
                return false;
            }
            final byte[] newRaw = new byte[available + this.rawInBuff.length];
            System.arraycopy(this.rawInBuff, 0, newRaw, 0, this.rawInBuff.length);
            int bytesRead;
            if (available != 0) {
                this.lastReadTime = System.currentTimeMillis();
                bytesRead = this.inputStream.read(newRaw, this.rawInBuff.length, available);
                if (bytesRead != available) {
                    System.err.println("TREMENDOUS EMERGENCY");
                }
            }

            this.rawInBuff = newRaw;

            final byte[] packets = readAllFrames(this.rawInBuff);

            final int numBytesUnread = this.dataEnd - this.readPtr;
            final int available2 = packets == null ? 0 : packets.length;

            final byte[] newInBuf = new byte[numBytesUnread + available2];
            if (numBytesUnread != 0) {
                System.arraycopy(this.inBuf, this.readPtr, newInBuf, 0, numBytesUnread);
            }

            if (available2 != 0) {
                this.lastReadTime = System.currentTimeMillis();
                bytesRead = available2;
                System.arraycopy(packets, 0, newInBuf, numBytesUnread, available2);
                this.dataEnd += bytesRead;
            } else {
                if ((time - this.lastReadTime) >= this.pingTimeout) {
                    return false;
                }
            }
            this.inBuf = newInBuf;

            this.dataEnd = this.inBuf.length;
            this.readPtr = 0;
        } catch (final IOException ex) {
            ex.printStackTrace();
            result = false;
        }
        return result;
    }

}
