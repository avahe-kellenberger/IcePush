package com.glgames.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.glgames.shared.InterthreadQueue;

public class InternetRelayChat implements Runnable {
	public static InterthreadQueue<String> msgs = new InterthreadQueue<String>();
	public static String nick;
	
	private static Socket s;
	private static BufferedWriter bw;
	private static BufferedReader br;
	private static String server;
	private static String channel;
	private static int port;
	
	public InternetRelayChat(String s, int p, String c, String n) {
		server = s;
		port = p;
		channel = c;
		nick = n;
	}
	
	public void run() {
		try {
			s = new Socket(server, port);
			bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			bw.write("NICK " + nick + " \n");
			bw.write("USER IcePush * * :IcePush Client Server\n");
			bw.write("JOIN " + channel + "\n");
			bw.flush();
			
			String input;
			while((input = br.readLine()) != null) {
				//System.out.println(input);
				if(input.startsWith("PING")) {
					bw.write(input.replace("PING", "PONG") + "\n");
					bw.flush();
				}
				String[] partsColon = input.split(":", 3);
				String[] partsSpace = input.split(" ");
				String cmd = partsSpace[1].toUpperCase();
				if(cmd.equals("PRIVMSG")) {
					String from = partsSpace[0].split("!")[0].substring(1);
					String msg = partsColon[2];
					if(!msg.contains("\u0001"))
						msgs.push("<" + from + "> " + msg);
				} else if(cmd.equals("KICK") || cmd.equals("INVITE")) {
					bw.write("JOIN " + channel + "\n");
					bw.flush();
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

	public static void logout() throws Exception {
		bw.flush();
		bw.close();
		br.close();
		s.close();
		br = null;
		bw = null;
		s = null;
	}
}
