package net.threesided.server;

import net.threesided.shared.InterthreadQueue;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.zip.Adler32;

public class UpdateServer extends Thread {

    private File indexDir;
    private ArrayList<Entry> index;
    private int dirNameLen;

    public boolean run;

    private InterthreadQueue<Socket> incomingConnections;
    private Con[] connections;

    private Adler32 adler;

    public UpdateServer(File file) {
        this.indexDir = file;
        this.dirNameLen = indexDir.getPath().length();
        this.adler = new Adler32();
        this.index = new ArrayList<>();
        this.incomingConnections = new InterthreadQueue<>();
        this.connections = new Con[10];
    }

    public void run() {
        this.loadFile(indexDir);
        this.serveUpdates();
    }

    private void loadFile(final File file) {
        if (file.isDirectory()) {
            String name = file.getPath().substring(dirNameLen);
            if (!name.isEmpty()) {
                if (name.charAt(name.length() - 1) != File.separatorChar)
                    name += File.separatorChar;
                System.out.println("Directory: " + name);
                Entry e = new Entry();
                e.name = name;
                this.index.add(e);
            }
            final File[] files = file.listFiles();
            if (files != null) {
                for (final File f : files) {
                    this.loadFile(f);
                }
            }
        } else {
            final Entry e = new Entry();
            e.name = file.getPath().substring(dirNameLen); // This is a very dangerous game
            e.modified = file.lastModified();
            final byte[] data = new byte[(int) file.length()];
            try {
                InputStream in = new FileInputStream(file);
                in.read(data);
                in.close();
            } catch (IOException ioe) {
                System.out.println("Error loading index!");
            }

            e.data = data;
            this.adler.reset();
            this.adler.update(data);
            e.crc = (int) adler.getValue();

            System.out.println("Loaded entry: " + e.name + "\tsize: " + e.data.length);
            index.add(e);
        }
    }

    private byte[] i2b(int i) {
        final byte[] b = new byte[4];
        b[0] = (byte) ((i >> 24) & 0xff);
        b[1] = (byte) ((i >> 16) & 0xff);
        b[2] = (byte) ((i >> 8) & 0xff);
        b[3] = (byte) (i & 0xff);
        return b;
    }

    @SuppressWarnings("unused")
    private int b2i(byte[] b) {
        return ((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16) | ((b[2] & 0xff) << 8) | (b[3] & 0xff);
    }

    private void serveUpdates() {
        while (this.run) {
            final Socket sock = incomingConnections.pull();
            if (sock != null) {
                int i = -1;
                try {
                    sock.setSoTimeout(5000);
                    for (int j = 0; j < connections.length; j++)
                        if (connections[j] == null) {
                            i = j;
                            break;
                        }

                    if (i != -1) {
                        this.connections[i] = new Con(sock, i);
                        sendIndex(this.connections[i].out);
                    } else {
                        System.out.println("Update backlog is full!");
                        sock.close();
                    }
                } catch (IOException ioe) {
                    tryClose(sock);
                    if (i != -1) {
                        this.connections[i] = null;
                    }
                }
            }

            for (final Con con : connections) {

                if (con == null) {
                    continue;
                }

                try {
                    if (con.in.available() > 0) {
                        int file = con.in.read();
                        con.lastReadTime = System.currentTimeMillis();
                        System.out.println("File " + file + " requested");
                        if (file >= 0 && file < index.size()) {
                            Entry e = index.get(file);
                            if (e.data != null)
                                // This should never happen
                                this.sendFile(con.out, e);
                        } else {
                            this.tryClose(con.sock);
                            this.connections[con.id] = null;
                            System.out.println("Someone tried to Hack us!!!!!!!!!" + con.sock.toString());
                        }
                    }

                    if (System.currentTimeMillis() - con.lastReadTime > 20000) {
                        System.out.println("Over 20 seconds since data was read:" + con.sock.toString());
                        tryClose(con.sock);
                        connections[con.id] = null;
                    }
                } catch (IOException ioe) {
                    tryClose(con.sock);
                    connections[con.id] = null;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}
        }
    }

    private void sendIndex(final OutputStream outs) throws IOException {
        final DataOutputStream out = new DataOutputStream(outs);
        out.writeInt(this.index.size());
        for (final Entry e : this.index) {
            out.writeUTF(e.name);
            out.writeLong(e.modified);
            out.writeInt(e.crc);
        }
    }

    private void sendFile(final OutputStream o, final Entry e) throws IOException {
        final byte[] b = i2b(e.data.length);
        o.write(b);
        o.write(e.data);
    }

    private void tryClose(Socket sock) {
        try {
            sock.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class Entry {
        String name;
        long modified;
        int crc;
        byte[] data;
    }

    private class Con {
        Socket sock;
        InputStream in;
        OutputStream out;
        final int id;
        long lastReadTime;

        /**
         *
         * @param sock
         * @param id
         * @throws IOException
         */
        Con(Socket sock, int id) throws IOException {
            this.sock = sock;
            this.in = sock.getInputStream();
            this.out = sock.getOutputStream();
            this.id = id;
            this.lastReadTime = System.currentTimeMillis();
        }
    }

}
