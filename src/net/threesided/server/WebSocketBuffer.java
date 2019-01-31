package net.threesided.server;

import java.io.*;
import java.net.*;
import java.security.*;
import net.threesided.shared.PacketBuffer;

public class WebSocketBuffer extends PacketBuffer {

    private String responseHeader =
            "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n";
    private int headerMax = 10000;
    private byte[] header = new byte[headerMax];
    private boolean headerRead = false;
    private long startTime;
    private long maxTime = 5000;
    private int headerPos = 0;

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

        if (i == 62) return '+';
        if (i == 63) return '/';

        return '~';
    }

    public int openPacket() {
        if ((readPtr + 3) > dataEnd) {
            // System.out.println("(1) readPtr = " + readPtr + " dataEnd = " + dataEnd);
            return -1; // Smallest valid packet size is 3
        }
        int size = readShort();
        if (size < 0) {
            // System.out.println("(2) size = " + size);
            readPtr -= 2;
            return -1; // hax
        }
        readPtr -= 2; // Reset the read ptr in case the packet is incomplete
        pktEnd = readPtr + size; // end = current + size;
        if (pktEnd > dataEnd) {
            return -1;
        }
        readPtr += 2; // Skip the length bytes
        int op = readByte();
        if (op == PING) return -1;
        return op;
    }

    private String base64(byte[] b) {
        String r = "";
        int i = 0, c = 0;
        int bits = 0;

        while (i != b.length) {
            c = i % 3;

            if (c == 0) {
                r = r + get64((b[i] >> 2) & 63);
                bits = (b[i] & 3) << 4;
            }

            if (c == 1) {
                r = r + get64(bits | ((b[i] >> 4) & 15));
                bits = (b[i] & 15) << 2;
            }

            if (c == 2) {
                r = r + get64(bits | ((b[i] >> 6) & 3));
                r = r + get64(b[i] & 63);
                bits = 0;
            }
            i++;
        }

        if (c != 2) r = r + get64(bits);

        if (c == 1) r = r + '=';
        if (c == 0) r = r + "==";
        return r;
    }

    public int available() {
        return inBuf.length - readPtr;
    }

    private String generateKey(String str) {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException n) {
        }

        String magic = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        String reply = str + magic;

        byte[] b = reply.getBytes();
        sha.update(b, 0, b.length);
        b = sha.digest();
        // System.out.println
        return base64(b);
    }

    private boolean readHeader(Socket s) throws IOException {
        byte[] buf = header;

        InputStream is = s.getInputStream();
        int a = is.available();

        if (a + headerPos > headerMax || a < 1) {
            System.out.println("Current headerPos = " + headerPos + " avail = " + a);
            return false;
        }

        int read = is.read(buf, headerPos, a);

        if (read == 0 || read == -1) {
            return false;
        }

        int end = headerPos = headerPos + read;

        byte[] term = "\r\n\r\n".getBytes();

        int headerEnd = -1;

        int i = 0;

        System.out.println("End = " + end);

        while (i < end) {
            if (buf[i] == term[0]
                    && buf[i + 1] == term[1]
                    && buf[i + 2] == term[2]
                    && buf[i + 3] == term[3]) {
                headerEnd = i;
            }
            i++;
        }

        i = 0;

        if (headerEnd != -1) { // This code only executes if the header ending was found  !!

            int rem = buf.length - (headerEnd + 4);
            byte[] nbuf = new byte[rem];
            System.arraycopy(buf, headerEnd + 4, nbuf, 0, rem);
            // c.buffer = nbuf;

            System.out.println("HEADER END = " + headerEnd);

            String header = new String(buf, 0, headerEnd);

            // System.out.println("header = " + header);

            String[] lines = header.split("\r\n");
            boolean foundKey = false;

            for (String line : lines) {

                // System.out.println("Header line " + i + ": " + line + "$");
                i++;
                String key = "Sec-WebSocket-Key: ";
                // System.out.println(line);
                String[] parts = line.split(key);

                if (parts.length == 2) {
                    String swsk = parts[1];

                    // System.out.println("Key =" + swsk);

                    String response =
                            responseHeader
                                    + "Sec-WebSocket-Accept: "
                                    + generateKey(swsk)
                                    + "\r\n\r\n";

                    s.getOutputStream().write(response.getBytes());
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

    public WebSocketBuffer(Socket s) {
        super(s);
        rawInBuff = new byte[0];
        startTime = System.currentTimeMillis();
        try {
            headerRead = readHeader(s);
            System.out.println("headerRead = " + headerRead);
        } catch (IOException ioe) {
            System.out.println("Error reading header");
            ioe.printStackTrace();
        }
    }

    byte[] readOneFrame(byte[] data, int pos, int end) {
        // System.out.println("Attempting frame read pos: " + pos + " end: " + end);

        if (end < pos + 2) return null;
        int opcode = data[pos] & 15;
        int mask = (data[pos + 1] >> 7) & 1;
        long dataLen = data[pos + 1] & 127;
        int bonus = 0;
        /*System.out.print("opcode = " + opcode);
        System.out.print(" mask = " + mask);
        System.out.print(" dataLen = " + dataLen);
        System.out.print(" fin = " + ((data[pos] >> 7)&1));*/

        int fin = (data[pos] >> 7) & 1;

        if (fin != 1) {
            // System.out.println("Incomplete WebSocket frame");
            // System.out.println("opcode = " + opcode + " data len = " + dataLen);
        }

        if (mask != 1) {
            throw new RuntimeException("Unmasked frame");
        }

        if (dataLen == 126) {
            bonus = 2;
            if (end < pos + 4) return null;
            dataLen = ((data[pos + 2] & 0xff) << 8) + (data[pos + 3] & 0xff);
            System.out.println("New Datalen: " + dataLen);
        } else if (dataLen == 127) {
            bonus = 8;
            if (end < pos + 10) return null;
            /*dataLen = (((long)(data[pos+2]&0xff))<<56) + ((long)(data[pos+3]&0xff)<<48) + ((long)(data[pos+4]&0xff)<<40) + ((long)(data[pos+5]&0xff)<<32) +
            ((data[pos+6]&0xff)<<24) + ((data[pos+7]&0xff)<<16) + ((data[pos+8]&0xff)<<8) + (data[pos+9]&0xff);*/
            System.out.println("(127) New Datalen: " + dataLen);
            int k = 0;
            while (k != 8) {
                System.out.println("byte" + k + ":" + (data[pos + 2 + k] & 255));
                k++;
            }
            // throw new RuntimeException();
        }

        if (end < (6 + pos + bonus + dataLen)) return null;

        byte[] key =
                new byte[] {
                    data[pos + 2 + bonus],
                    data[pos + 3 + bonus],
                    data[pos + 4 + bonus],
                    data[pos + 5 + bonus]
                };

        int i = 0;

        byte nbuf[] = new byte[(int) dataLen];

        while (i != dataLen) {
            nbuf[i] = (byte) (key[i % 4] ^ data[pos + 6 + i + bonus]);
            // System.out.println("Byte : " + i + ": " + (key[i%4] ^ data[6 + i]));
            i++;
        }

        nextPos = pos + 6 + (int) dataLen + bonus;

        // byte[] nbuf = new byte[c.buffer.length - (6 + len)];

        // System.arraycopy(c.buffer, 6 + len, nbuf, 0, c.buffer.length - (6 + len));
        // c.decoded = nbuf;
        // shutDown = true;
        // System.out.println("===== Read one Frame pos:" + pos + ", end:" + end + "  ===== nextPos
        // = " + nextPos);
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

    byte[] encodeData(byte[] data, int len) {
        int packs = len / 125;
        int tail = len % 125;
        byte[] out =
                new byte
                        [(packs) * (2 + 125)
                                + (tail == 0
                                        ? 0
                                        : (2
                                                + tail))]; // 125 is maximum size of a data frame
                                                           // without extended payload length bytes

        int i = 0;

        while (i != packs) {
            out[i * 127] = (byte) (((byte) 1) << 7) | (2); // opcode = 2 for binary data, FIN = 1
            out[i * 127 + 1] = 125; // Data length
            System.arraycopy(data, i * 125, out, i * 127 + 2, 125);
            i++;
        }

        if (tail != 0) {
            out[i * 127] = (byte) (((byte) 1) << 7) | (2);
            out[i * 127 + 1] = (byte) tail; // Data length is number of bytes remaining in the tail
            System.arraycopy(data, i * 125, out, i * 127 + 2, tail);
        }

        return out;
    }

    public boolean synch() {

        long time = System.currentTimeMillis();

        if (!headerRead) {
            try {
                headerRead = readHeader(sock);
            } catch (IOException ioe) {
                System.out.println("Error reading header");
                ioe.printStackTrace();
                return false;
            }

            if (!headerRead) {
                return (time - startTime) < maxTime;
            }
        }

        boolean result = true;

        // Write out all data in the out buffers
        try {
            if (time - lastWriteTime >= pingTime) {
                beginPacket(PING);
                endPacket();
            }

            if (writePtr != 0) {
                out.write(encodeData(outBuf, writePtr));
                out.flush();
                lastWriteTime = time;
            }
            writePtr = 0;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            result = false;
        }
        try {
            int available = in.available();
            if (available < 0) {
                System.out.println("Negative available: " + available);
                return false;
            }
            // System.out.println("Raw inbuf length = " + rawInBuff.length);
            byte[] newRaw = new byte[available + rawInBuff.length];

            // if (rawPtr != 0) {
            System.arraycopy(rawInBuff, 0, newRaw, 0, rawInBuff.length);
            // }

            int bytesRead = -55;

            if (available != 0) {
                lastReadTime = System.currentTimeMillis();
                // System.out.println("newRaw length = " + newRaw.length + " rawInbuff.length = " +
                // rawInBuff.length + " available: " + available);
                bytesRead = in.read(newRaw, rawInBuff.length, available);
                // dataEnd += bytesRead;
                if (bytesRead != available) System.out.println("TREMENDOUS EMERGENCY");
            }

            // System.out.println("Bytes read: >>>>" + bytesRead+"<<<<");

            rawInBuff = newRaw;

            byte[] packets = readAllFrames(rawInBuff);

            int numBytesUnread = dataEnd - readPtr;
            int available2 = packets == null ? 0 : packets.length;

            // System.out.println("inBuf.len:" + inBuf.length + ", rawInBuff.len:" +
            // rawInBuff.length + ", avail2:" + available2 + ", numBytesUnread:" + numBytesUnread +
            // ", dataEnd:" + dataEnd + ", readPtr:" + readPtr);

            byte[] newInBuf = new byte[numBytesUnread + available2];
            if (numBytesUnread != 0) {
                System.arraycopy(inBuf, readPtr, newInBuf, 0, numBytesUnread);
            }

            if (available2 != 0) {
                lastReadTime = System.currentTimeMillis();
                bytesRead = available2;
                System.arraycopy(packets, 0, newInBuf, numBytesUnread, available2);
                dataEnd += bytesRead;
            } else {
                if ((time - lastReadTime) >= pingTimeout) {
                    System.out.println("Ping timeout expired: " + (time - lastReadTime));
                    return false;
                }
            }
            inBuf = newInBuf;

            dataEnd = inBuf.length;
            readPtr = 0;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            result = false;
        }
        return result;
    }

    byte[]
            rawInBuff; // Contains any left over bytes from incoming websocket frames that are
                       // incomplete
    private int nextPos;
    public boolean readVer;
    public boolean readName;
}
