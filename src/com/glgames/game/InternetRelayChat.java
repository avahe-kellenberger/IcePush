package com.glgames.game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.glgames.shared.InterthreadQueue;

public class InternetRelayChat implements Runnable {
	public static InterthreadQueue<String> msgs = new InterthreadQueue<String>();
	
	private static BufferedWriter bw;
	private static BufferedReader br;
	private static String server;
	private static String channel;
	private static int port;
	
	public InternetRelayChat(String s, int p, String c) {
		server = s;
		port = p;
		channel = c;
	}
	
	public void run() {
		try {
			Socket s = new Socket(server, port);
			bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			bw.write("NICK IcePush" + (int) (Math.random() * 100) + "\n");
			bw.write("USER IcePush * * :IcePush Client\n");
			bw.write("JOIN " + channel + "\n");
			bw.flush();
			
			String input;
			while((input = br.readLine()) != null) {
				if(input.startsWith("PING")) {
					bw.write(input.replace("PING", "PONG"));
					bw.flush();
				}
				String[] partsColon = input.split(":", 3);
				String[] partsSpace = input.split(" ");
				if(partsSpace[1].equals("PRIVMSG")) {
					String from = partsSpace[0].split("!")[0].substring(1);
					String msg = partsColon[2];
					msgs.push(from + ": " + msg);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	} 
	
	public static void sendMessage(String message) {
		try {
			bw.write("PRIVMSG " + channel + " :" + message + "\n");
			bw.flush();
		} catch(Exception e) { }
	}
}
