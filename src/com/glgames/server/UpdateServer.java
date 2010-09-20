package com.glgames.server;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.zip.Adler32;

import com.glgames.shared.InterthreadQueue;

public class UpdateServer extends Thread {

	private File indexDir;
	//private int fileCount;
	private ArrayList<Entry> index;
	private int dirNameLen;

	InterthreadQueue<Socket> incomingConnections;
	private Con connections[];

	private Adler32 adler;

	public UpdateServer(File file) {
		indexDir = file;
		dirNameLen = indexDir.getPath().length();
		adler = new Adler32();
		index = new ArrayList<Entry>();
		incomingConnections = new InterthreadQueue<Socket>();
		connections = new Con[10];
	}

	public void run() {
		loadFile(indexDir);
		serveUpdates();
	}

	private void loadFile(File file) {
		if(file.isDirectory()) {
			String name = file.getPath().substring(dirNameLen);
			if(!name.isEmpty()) {
				if(name.charAt(name.length() - 1) != File.separatorChar) name += File.separatorChar;
				System.out.println("Directory: " + name);
				Entry e = new Entry();
				e.name = name;
				index.add(e);
			}
			File files[] = file.listFiles();
			for(File f : files) loadFile(f);
		} else {
			Entry e = new Entry();
			e.name = file.getPath().substring(dirNameLen);	// This is a very dangerous game
			e.modified = file.lastModified();
			byte data[] = new byte[(int)file.length()];
			try {
				InputStream in = new FileInputStream(file);
				in.read(data);
				in.close();
			} catch (IOException ioe) {
				System.out.println("Error loading index!");
			}

			e.data = data;
			adler.reset();
			adler.update(data);
			e.crc = (int)adler.getValue();

			System.out.println("Loaded entry: " + e.name + "\tsize: " + e.data.length);

			index.add(e);
		}
	}

	private byte[] i2b(int i) {
		byte b[] = new byte[4];
		b[0] = (byte)((i >> 24) & 0xff);
		b[1] = (byte)((i >> 16) & 0xff);
		b[2] = (byte)((i >> 8) & 0xff);
		b[3] = (byte)(i & 0xff);
		return b;
	}

	@SuppressWarnings("unused")
	private int b2i(byte b[]) {
		return ((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16) | ((b[2] & 0xff) << 8) | (b[3] & 0xff);
	}

	private void serveUpdates() {
		while(Server.run) {
			Socket sock = incomingConnections.pull();
			if(sock != null) {
				int i = -1;
				try {
					sock.setSoTimeout(5000);						// haqcors

				System.out.println("send size: " + sock.getSendBufferSize());
					for(int j = 0; j < connections.length; j++)
						if(connections[j] == null) {
							i = j;
							break;
						}

					if(i != -1) {
						connections[i] = new Con(sock, i);
						sendIndex(connections[i].out);
					} else {
						System.out.println("Update backlog is full!");
						sock.close();
					}
				} catch (IOException ioe) {
					tryClose(sock);
					if(i != -1) connections[i] = null;
				}
			}

			for(Con con : connections) {
				if(con == null) continue;
				try {
					if(con.in.available() > 0) {
						int file = con.in.read();
						con.lastReadTime = System.currentTimeMillis();
						System.out.println("File " + file + " requested");
						if(file >= 0 && file < index.size()) {
							Entry e = index.get(file);
							if(e.data != null) sendFile(con.out, e);				// this should nev er happen ..
						} else {									// HACQORS
							tryClose(con.sock);
							connections[con.id] = null;
							System.out.println("Someone tried to Hack us!!!!!!!!!" + con.sock.toString());
						}
					}

					long now = System.currentTimeMillis();
					if(now - con.lastReadTime > 20000) {
						System.out.println("Over 20 seconds since data was read:" + con.sock.toString());
						tryClose(con.sock);
						connections[con.id] = null;
					}
				} catch(IOException ioe) {
					tryClose(con.sock);
					connections[con.id] = null;
				}
			}
			try {
				Thread.sleep(500);
			} catch(InterruptedException ie) {

			}
		}
	}

	private void sendIndex(OutputStream outs) throws IOException {
		DataOutputStream out = new DataOutputStream(outs);

		out.writeInt(index.size());

		for(Entry e : index) {
			out.writeUTF(e.name);
			out.writeLong(e.modified);
			out.writeInt(e.crc);
		}
	}

	private void sendFile(OutputStream o, Entry e) throws IOException {
		byte[] b = i2b(e.data.length);
		System.out.println("Sending " + e.name);
		o.write(b);
		o.write(e.data);
	}

	private void tryClose(Socket sock) {
		try {
			sock.close();
		} catch(IOException ioe) {
			System.out.println("Failed to close socket!");
			ioe.printStackTrace();
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

		Con(Socket sock, int id) throws IOException {
			this.sock = sock;
			in = sock.getInputStream();
			out = sock.getOutputStream();
			this.id = id;
			lastReadTime = System.currentTimeMillis();
		}
	}
}