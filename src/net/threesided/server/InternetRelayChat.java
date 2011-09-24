package net.threesided.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import net.threesided.shared.InterthreadQueue;

public class InternetRelayChat implements Runnable {
	private static final String[] controllers = { "_^_", "Tekk", "Evil_", "linkmaster03", "Dezired`" };
	public static InterthreadQueue<String> msgs = new InterthreadQueue<String>();		// Messages queued by the application to be sent to IRC
	private static InterthreadQueue<String> inputs = new InterthreadQueue<String>();	// Input sent from IRC to be queued until the application calls processInput()
	public static InterthreadQueue<String> kicks = new InterthreadQueue<String>();		// Kick commands parsed out of processInput() to be returned to the application
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
	
	public void run() {				// This method runs on its own dedicated thread
		try {
			s = new Socket(server, port);
			bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			bw.write("NICK " + nick + " \n");
			bw.write("USER IcePush * * :IcePush Client Server\n");
			bw.write("JOIN " + channel + "\n");
			bw.flush();
			
			String input;
			while((input = br.readLine()) != null) inputs.push(input);
		} catch(Exception e) {
			e.printStackTrace();
		}
	} 
	
	public static void processInput() {		// This method runs on the application thread
		String input;
		while((input = inputs.pull()) != null) {
			if(input.startsWith("PING")) {
				try {
					bw.write(input.replace("PING", "PONG") + "\n");
					bw.flush();
					continue;
				} catch (java.io.IOException ioe) {
					ioe.printStackTrace();
				}
			}

			try {
				String[] partsColon = input.split(":", 3);
				String[] partsSpace = input.split(" ");
				String cmd = partsSpace[1].toUpperCase();
				if(cmd.equals("PRIVMSG")) {
					String from = partsSpace[0].split("!")[0].substring(1);
					String msg = partsColon[2];
					if(msg.startsWith(".")) handleCommand(from, msg.substring(1));
					if(!msg.contains("\u0001")) {
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
	}
	
	private static void handleCommand(String from, String msg) {
		String[] args = msg.split(" ");
		System.out.println("Got command " +args[0] + " from " + from);
		boolean auth = false;
		for(String n : controllers)
			if(from.toLowerCase().equals(n.toLowerCase()))
				auth = true;
		if(!auth) {
			return;
		}

		System.out.println("auth1: " + auth);
		// This commented out block needs to be fixed properly, but can't be without coming up with a better threading model
		/*try {
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
			return;
		}*/

		if (args[0].equals("kick")) {
			if(args.length > 1) kicks.push(args[1]);
			else	sendMessage("Not enough arguments for command");
		}
	}

	private static void processLine(String input) {
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
