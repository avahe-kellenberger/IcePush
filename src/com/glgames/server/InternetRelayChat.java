package com.glgames.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.glgames.shared.InterthreadQueue;

public class InternetRelayChat implements Runnable {
	private static final String[] controllers = { "_^_", "Tekk", "Someone67", "linkmaster03", "Dezired`" };
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
				} else processLine(input);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	} 
	
	private void processLine(String input) {
		try {
			String[] partsColon = input.split(":", 3);
			String[] partsSpace = input.split(" ");
			String cmd = partsSpace[1].toUpperCase();
			if(cmd.equals("PRIVMSG")) {
				String from = partsSpace[0].split("!")[0].substring(1);
				String msg = partsColon[2];
				if(msg.startsWith("."))
					handleCommand(from, msg.substring(1));
				if(!msg.contains("\u0001"))
					msgs.push("<" + from + "> " + msg);
			} else if(cmd.equals("KICK") || cmd.equals("INVITE")) {
				bw.write("JOIN " + channel + "\n");
				bw.flush();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void handleCommand(String from, String msg) {
		String[] args = msg.split(" ");
		System.out.println("Got command " +args[0] + " from " + from);
		boolean auth = false;
		for(String n : controllers)
			if(from.toLowerCase().equals(n.toLowerCase()))
				auth = true;
		if(!auth) {
			return;
		}

		System.out.println("Got command " +args[0] + " from " + from);
		System.out.println("auth1: " + auth);
		try {
			bw.write("PRIVMSG NickServ :STATUS " + from + "\n");
			bw.flush();
			String r;
			while((r = br.readLine()) != null) {
				System.out.println(r);
				String[] partsColon = r.split(":", 3);
				String[] partsSpace = r.split(" ");
				String cmd = partsSpace[1].toUpperCase();
				if(!cmd.equals("NOTICE"))
					processLine(r);
				String ns = partsSpace[0].split("!")[0].substring(1);
				String nsmsg = partsColon[2];
				if(!ns.toLowerCase().equals("nickserv"))
					processLine(r);
				if(!nsmsg.contains("STATUS") || !(nsmsg.endsWith("2") || nsmsg.endsWith("3"))) {
					auth = false;
					break;
				} else break;
			}
		} catch(Exception e) {
			e.printStackTrace();
			auth = false;
		}
		if(!auth) {
			try {
				bw.write("PRIVMSG " + channel + " :You are not allowed to use this command\n");
				bw.flush();
			} catch (Exception e) { }
			return;
		}
		if (args[0].equals("kick")) {
			if(args.length > 1) {
				boolean found = false;
				for (Player p : Server.players)
					if (p != null)
						if (p.username.toLowerCase().equals(args[1].toLowerCase())) {
							Server.logoutPlayer(p);
							found = true;
						}
				try {
					if(found)
						bw.write("PRIVMSG " + channel + " :Player " + args[1] + " has been kicked.\n");
					bw.flush();
				} catch (Exception e) { }
			} else {
				try {
					bw.write("PRIVMSG " + channel + " :Not enough arguments for command\n");
					bw.flush();
				} catch (Exception e) { }
			}
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
